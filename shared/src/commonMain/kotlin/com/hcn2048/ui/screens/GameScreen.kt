package com.hcn2048.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcn2048.ads.AdBanner
import com.hcn2048.ads.AdManager
import com.hcn2048.billing.BillingOutcome
import com.hcn2048.resources.Res
import com.hcn2048.resources.ads_not_available
import com.hcn2048.resources.new_game
import com.hcn2048.resources.purchase_canceled
import com.hcn2048.resources.purchase_error
import com.hcn2048.resources.purchase_success
import com.hcn2048.resources.restore_not_found
import com.hcn2048.resources.undo
import com.hcn2048.resources.undo_with_ad
import com.hcn2048.ui.GameEvent
import com.hcn2048.ui.GameUiState
import com.hcn2048.ui.GameViewModel
import com.hcn2048.ui.components.GameBoardView
import com.hcn2048.ui.components.GameOverDialog
import com.hcn2048.ui.components.HowToPlayDialog
import com.hcn2048.ui.components.RemoveAdsDialog
import com.hcn2048.ui.components.ScoreHeader
import com.hcn2048.ui.components.SettingsDialog
import com.hcn2048.ui.components.WinDialog
import com.hcn2048.ui.theme.GameTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    ads: AdManager,
    state: GameUiState,
) {
    val theme = GameTheme.byId(state.themeId)
    val snackbarHostState = remember { SnackbarHostState() }

    // Resolved here because the event collector below is not a composable scope.
    val purchaseSuccess = stringResource(Res.string.purchase_success)
    val purchaseCanceled = stringResource(Res.string.purchase_canceled)
    val purchaseError = stringResource(Res.string.purchase_error)
    val restoreNotFound = stringResource(Res.string.restore_not_found)
    val adUnavailable = stringResource(Res.string.ads_not_available)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is GameEvent.ShowRewardedAd -> {
                    ads.showRewarded(
                        onReward = { viewModel.onRewardEarned(event.reward) },
                        onUnavailable = { viewModel.onRewardedAdUnavailable() },
                    )
                    null
                }

                GameEvent.ShowInterstitial -> {
                    ads.showInterstitial()
                    null
                }

                is GameEvent.PurchaseFinished -> when (event.outcome) {
                    BillingOutcome.Success -> purchaseSuccess
                    BillingOutcome.Canceled -> purchaseCanceled
                    BillingOutcome.NotFound -> restoreNotFound
                    else -> purchaseError
                }

                GameEvent.RewardedAdUnavailable -> adUnavailable
            }
            message?.let { snackbarHostState.showSnackbar(it) }
        }
    }

    if (!state.loaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = theme.textPrimary)
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = theme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            ScoreHeader(score = state.board.score, best = state.bestScore, theme = theme)
            Spacer(Modifier.height(20.dp))
            GameBoardView(
                board = state.board,
                theme = theme,
                hapticsEnabled = state.hapticsEnabled,
                onSwipe = viewModel::onSwipe,
                onGhostsConsumed = viewModel::onGhostsAnimationFinished,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = viewModel::onUndoRequested,
                    enabled = state.canUndo,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    Text(
                        text = if (state.undoIsFree) {
                            stringResource(Res.string.undo)
                        } else {
                            stringResource(Res.string.undo_with_ad)
                        },
                        maxLines = 1,
                        fontSize = 14.sp,
                    )
                }
                Button(
                    onClick = viewModel::onNewGame,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.new_game),
                        maxLines = 1,
                        fontSize = 14.sp,
                    )
                }
                OutlinedButton(
                    onClick = viewModel::onSettingsOpen,
                    modifier = Modifier.height(48.dp),
                ) {
                    Text(text = "⚙", fontSize = 18.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            if (!state.adsRemoved) {
                AdBanner(Modifier.padding(bottom = 6.dp))
            }
        }
    }

    if (state.showWinDialog) {
        WinDialog(
            onKeepPlaying = viewModel::onContinueAfterWin,
            onNewGame = viewModel::onNewGame,
        )
    }

    if (state.showGameOverDialog) {
        GameOverDialog(
            score = state.board.score,
            reviveAvailable = state.reviveAvailable,
            onRevive = viewModel::onReviveRequested,
            onNewGame = viewModel::onNewGame,
        )
    }

    if (state.showSettingsDialog) {
        SettingsDialog(
            themeId = state.themeId,
            hapticsEnabled = state.hapticsEnabled,
            adsRemoved = state.adsRemoved,
            privacyOptionsRequired = state.privacyOptionsRequired,
            onThemeSelected = viewModel::onThemeSelected,
            onHapticsToggled = viewModel::onHapticsToggled,
            onRemoveAds = viewModel::onRemoveAdsOpen,
            onRestorePurchases = viewModel::onRestorePurchases,
            onPrivacyOptions = viewModel::onPrivacyOptions,
            onHowToPlay = viewModel::onHowToPlayOpen,
            onDismiss = viewModel::onSettingsClose,
        )
    }

    if (state.showRemoveAdsDialog) {
        RemoveAdsDialog(
            price = state.removeAdsPrice,
            onBuy = viewModel::onRemoveAdsConfirm,
            onRestore = viewModel::onRestorePurchases,
            onDismiss = viewModel::onRemoveAdsClose,
        )
    }

    if (state.showHowToPlayDialog) {
        HowToPlayDialog(onDismiss = viewModel::onHowToPlayClose)
    }
}
