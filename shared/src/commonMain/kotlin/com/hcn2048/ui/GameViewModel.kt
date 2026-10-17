package com.hcn2048.ui

import com.hcn2048.ads.AdManager
import com.hcn2048.billing.BillingManager
import com.hcn2048.billing.BillingOutcome
import com.hcn2048.data.PreferencesRepository
import com.hcn2048.engine.GameEngine
import com.hcn2048.engine.GameSerializer
import com.hcn2048.model.Direction
import com.hcn2048.model.GameBoard
import com.hcn2048.model.GameStatus
import com.hcn2048.review.ReviewPrompt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the whole game session: state, progression, persistence and monetization rules.
 *
 * Monetization rules baked in:
 *  - 1 free undo per game, further undos require a rewarded ad;
 *  - 1 revive per game (rewarded ad) when the board is locked;
 *  - interstitial every [INTERSTITIAL_EVERY] game overs (never mid-game);
 *  - remove-ads one-time purchase suppresses banner + interstitial (rewarded stays optional).
 */
class GameViewModel(
    private val engine: GameEngine,
    private val preferences: PreferencesRepository,
    private val ads: AdManager,
    private val billing: BillingManager,
    private val reviewPrompt: ReviewPrompt,
    private val scope: CoroutineScope,
) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = Channel<GameEvent>(Channel.BUFFERED)
    val events: Flow<GameEvent> = _events.receiveAsFlow()

    private var previousBoard: GameBoard? = null
    private var savedBest = 0
    private var gameOverCount = 0
    private var reviveUsed = false
    private var interstitialPending = false

    init {
        scope.launch {
            val restored = GameSerializer.deserialize(preferences.savedGame.first())
            val board = when {
                restored == null || restored.tiles.isEmpty() -> engine.newGame()
                !engine.canPlay(restored) -> restored.copy(status = GameStatus.GameOver)
                else -> restored
            }
            val best = preferences.bestScore.first()
            savedBest = maxOf(best, board.score)
            gameOverCount = preferences.gameOverCount.first()
            val gameOver = board.status == GameStatus.GameOver
            _uiState.update {
                it.copy(
                    loaded = true,
                    board = board,
                    bestScore = savedBest,
                    themeId = preferences.themeId.first(),
                    hapticsEnabled = preferences.hapticsEnabled.first(),
                    adsRemoved = preferences.adsRemoved.first(),
                    undoIsFree = !preferences.freeUndoUsed.first(),
                    showGameOverDialog = gameOver,
                    reviveAvailable = gameOver,
                )
            }
        }
        scope.launch {
            preferences.adsRemoved.collect { removed ->
                _uiState.update { it.copy(adsRemoved = removed) }
            }
        }
        scope.launch {
            billing.owned.collect { owned ->
                if (owned) {
                    _uiState.update { it.copy(adsRemoved = true) }
                    preferences.setAdsRemoved(true)
                }
            }
        }
        scope.launch {
            billing.priceLabel.collect { price ->
                _uiState.update { it.copy(removeAdsPrice = price) }
            }
        }
        scope.launch {
            ads.privacyOptionsRequired.collect { required ->
                _uiState.update { it.copy(privacyOptionsRequired = required) }
            }
        }
        billing.refresh()
        ads.preload()
    }

    // --- Gameplay ---

    fun onSwipe(direction: Direction) {
        val current = _uiState.value
        if (!current.loaded) return
        val board = current.board
        if (board.status == GameStatus.GameOver) return

        val next = engine.move(board, direction)
        if (next === board) return

        previousBoard = board
        val best = maxOf(current.bestScore, next.score)
        _uiState.update {
            it.copy(
                board = next,
                bestScore = best,
                canUndo = true,
                showWinDialog = next.status == GameStatus.Won,
                showGameOverDialog = next.status == GameStatus.GameOver,
                reviveAvailable = next.status == GameStatus.GameOver && !reviveUsed,
            )
        }
        persist(next)
        if (next.status == GameStatus.Won) maybeRequestReview()
        if (next.status == GameStatus.GameOver) {
            gameOverCount += 1
            interstitialPending = gameOverCount % INTERSTITIAL_EVERY == 0
            scope.launch { preferences.setGameOverCount(gameOverCount) }
        }
    }

    /** Called by the UI after the merge ghost animation completes. */
    fun onGhostsAnimationFinished() {
        _uiState.update { state ->
            if (state.board.ghosts.isEmpty()) {
                state
            } else {
                state.copy(board = state.board.copy(ghosts = emptyList()))
            }
        }
    }

    // --- Undo (1 free per game, then rewarded ad) ---

    fun onUndoRequested() {
        val state = _uiState.value
        if (previousBoard == null || !state.canUndo) return
        if (state.undoIsFree) {
            performUndo()
        } else {
            _events.trySend(GameEvent.ShowRewardedAd(RewardType.Undo))
        }
    }

    private fun performUndo() {
        val board = previousBoard ?: return
        previousBoard = null
        scope.launch { preferences.setFreeUndoUsed(true) }
        _uiState.update {
            it.copy(
                board = board.copy(ghosts = emptyList()),
                canUndo = false,
                undoIsFree = false,
                showGameOverDialog = false,
                showWinDialog = false,
                reviveAvailable = false,
            )
        }
        persist(board)
    }

    // --- Revive (once per game, via rewarded ad) ---

    fun onReviveRequested() {
        if (!_uiState.value.reviveAvailable) return
        _events.trySend(GameEvent.ShowRewardedAd(RewardType.Revive))
    }

    private fun performRevive() {
        val state = _uiState.value
        val revived = engine.revive(state.board)
        if (revived === state.board) return
        reviveUsed = true
        interstitialPending = false
        _uiState.update {
            it.copy(
                board = revived,
                reviveAvailable = false,
                showGameOverDialog = false,
            )
        }
        persist(revived)
    }

    fun onRewardEarned(reward: RewardType) {
        when (reward) {
            RewardType.Undo -> performUndo()
            RewardType.Revive -> performRevive()
        }
    }

    fun onRewardedAdUnavailable() {
        _events.trySend(GameEvent.RewardedAdUnavailable)
    }

    // --- Game flow ---

    fun onNewGame() {
        val wasGameOver = _uiState.value.showGameOverDialog
        previousBoard = null
        reviveUsed = false
        val board = engine.newGame()
        _uiState.update {
            it.copy(
                board = board,
                canUndo = false,
                undoIsFree = true,
                showWinDialog = false,
                showGameOverDialog = false,
                reviveAvailable = false,
            )
        }
        scope.launch {
            preferences.setFreeUndoUsed(false)
            preferences.saveGame(GameSerializer.serialize(board))
        }
        if (wasGameOver) showPendingInterstitial()
    }

    fun onContinueAfterWin() {
        val board = _uiState.value.board
        val resumed = if (engine.canPlay(board)) GameStatus.Playing else GameStatus.GameOver
        val updated = board.copy(status = resumed)
        _uiState.update {
            it.copy(
                board = updated,
                showWinDialog = false,
                showGameOverDialog = resumed == GameStatus.GameOver,
                reviveAvailable = resumed == GameStatus.GameOver && !reviveUsed,
            )
        }
        persist(updated)
    }

    // --- Settings ---

    fun onSettingsOpen() = _uiState.update { it.copy(showSettingsDialog = true) }

    fun onSettingsClose() = _uiState.update { it.copy(showSettingsDialog = false) }

    fun onThemeSelected(themeId: String) {
        _uiState.update { it.copy(themeId = themeId) }
        scope.launch { preferences.setThemeId(themeId) }
    }

    fun onHapticsToggled(enabled: Boolean) {
        _uiState.update { it.copy(hapticsEnabled = enabled) }
        scope.launch { preferences.setHapticsEnabled(enabled) }
    }

    fun onHowToPlayOpen() = _uiState.update { it.copy(showHowToPlayDialog = true) }

    fun onHowToPlayClose() = _uiState.update { it.copy(showHowToPlayDialog = false) }

    // --- Monetization ---

    fun onRemoveAdsOpen() {
        _uiState.update { it.copy(showRemoveAdsDialog = true) }
        billing.refresh()
    }

    fun onRemoveAdsClose() = _uiState.update { it.copy(showRemoveAdsDialog = false) }

    fun onRemoveAdsConfirm() {
        billing.purchaseRemoveAds { outcome ->
            if (outcome is BillingOutcome.Success) {
                _uiState.update { it.copy(adsRemoved = true, showRemoveAdsDialog = false) }
                scope.launch { preferences.setAdsRemoved(true) }
            }
            scope.launch { _events.send(GameEvent.PurchaseFinished(outcome)) }
        }
    }

    fun onRestorePurchases() {
        billing.restorePurchases { outcome ->
            scope.launch { _events.send(GameEvent.PurchaseFinished(outcome)) }
        }
    }

    fun onPrivacyOptions() {
        ads.showPrivacyOptionsForm()
    }

    // --- Internals ---

    private fun showPendingInterstitial() {
        if (!interstitialPending) return
        interstitialPending = false
        if (!_uiState.value.adsRemoved) _events.trySend(GameEvent.ShowInterstitial)
    }

    private fun maybeRequestReview() {
        scope.launch {
            if (!preferences.reviewRequested.first()) {
                preferences.setReviewRequested(true)
                reviewPrompt.requestReview()
            }
        }
    }

    private fun persist(board: GameBoard) {
        scope.launch {
            preferences.saveGame(GameSerializer.serialize(board))
            val best = maxOf(savedBest, board.score)
            if (best > savedBest) {
                savedBest = best
                preferences.setBestScore(best)
            }
        }
    }

    companion object {
        private const val INTERSTITIAL_EVERY = 3
    }
}
