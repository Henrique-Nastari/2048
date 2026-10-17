package com.hcn2048.ui

import com.hcn2048.billing.BillingOutcome
import com.hcn2048.model.GameBoard

/** Everything the game screen renders. */
data class GameUiState(
    val loaded: Boolean = false,
    val board: GameBoard = GameBoard(),
    val bestScore: Int = 0,
    val themeId: String = "CLASSIC",
    val hapticsEnabled: Boolean = true,
    val adsRemoved: Boolean = false,
    val canUndo: Boolean = false,
    val undoIsFree: Boolean = true,
    val reviveAvailable: Boolean = false,
    val showWinDialog: Boolean = false,
    val showGameOverDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showRemoveAdsDialog: Boolean = false,
    val showHowToPlayDialog: Boolean = false,
    val removeAdsPrice: String? = null,
    val privacyOptionsRequired: Boolean = false,
)

/** What a rewarded ad grants when fully watched. */
enum class RewardType { Undo, Revive }

/** One-shot side effects consumed by the UI (ads, snackbars). */
sealed interface GameEvent {
    data class ShowRewardedAd(val reward: RewardType) : GameEvent
    data object ShowInterstitial : GameEvent
    data class PurchaseFinished(val outcome: BillingOutcome) : GameEvent
    data object RewardedAdUnavailable : GameEvent
}
