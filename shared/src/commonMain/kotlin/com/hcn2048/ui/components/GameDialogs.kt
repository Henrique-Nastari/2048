package com.hcn2048.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.hcn2048.resources.Res
import com.hcn2048.resources.buy
import com.hcn2048.resources.buy_for
import com.hcn2048.resources.close
import com.hcn2048.resources.game_over_score
import com.hcn2048.resources.game_over_title
import com.hcn2048.resources.haptics
import com.hcn2048.resources.how_to_play
import com.hcn2048.resources.how_to_play_text
import com.hcn2048.resources.keep_playing
import com.hcn2048.resources.new_game
import com.hcn2048.resources.not_now
import com.hcn2048.resources.privacy_options
import com.hcn2048.resources.remove_ads
import com.hcn2048.resources.remove_ads_desc
import com.hcn2048.resources.remove_ads_title
import com.hcn2048.resources.restore_purchases
import com.hcn2048.resources.revive_with_ad
import com.hcn2048.resources.settings
import com.hcn2048.resources.theme
import com.hcn2048.resources.theme_classic
import com.hcn2048.resources.theme_dark
import com.hcn2048.resources.theme_neon
import com.hcn2048.resources.win_text
import com.hcn2048.resources.win_title
import com.hcn2048.ui.theme.GameTheme
import org.jetbrains.compose.resources.stringResource

@Composable
private fun GameDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

@Composable
fun WinDialog(onKeepPlaying: () -> Unit, onNewGame: () -> Unit) {
    GameDialog(onDismiss = onKeepPlaying) {
        Text(
            text = stringResource(Res.string.win_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.win_text, 2048),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onKeepPlaying, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.keep_playing))
        }
        TextButton(onClick = onNewGame, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.new_game))
        }
    }
}

@Composable
fun GameOverDialog(
    score: Int,
    reviveAvailable: Boolean,
    onRevive: () -> Unit,
    onNewGame: () -> Unit,
) {
    // Modal on purpose: the board is locked, so the player must pick
    // "revive" or "new game" instead of dismissing back to a dead board
    // with no way to reopen this dialog.
    GameDialog(onDismiss = { }) {
        Text(
            text = stringResource(Res.string.game_over_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(text = stringResource(Res.string.game_over_score, score))
        Spacer(Modifier.height(20.dp))
        if (reviveAvailable) {
            Button(onClick = onRevive, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.revive_with_ad))
            }
            Spacer(Modifier.height(8.dp))
        }
        Button(onClick = onNewGame, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.new_game))
        }
    }
}

@Composable
fun SettingsDialog(
    themeId: String,
    hapticsEnabled: Boolean,
    adsRemoved: Boolean,
    privacyOptionsRequired: Boolean,
    onThemeSelected: (String) -> Unit,
    onHapticsToggled: (Boolean) -> Unit,
    onRemoveAds: () -> Unit,
    onRestorePurchases: () -> Unit,
    onPrivacyOptions: () -> Unit,
    onHowToPlay: () -> Unit,
    onDismiss: () -> Unit,
) {
    GameDialog(onDismiss = onDismiss) {
        Text(
            text = stringResource(Res.string.settings),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.haptics),
                modifier = Modifier.weight(1f),
            )
            Switch(checked = hapticsEnabled, onCheckedChange = onHapticsToggled)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.theme),
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GameTheme.ALL.forEach { gameTheme ->
                val label = themeLabel(gameTheme.id)
                if (gameTheme.id == themeId) {
                    Button(
                        onClick = { onThemeSelected(gameTheme.id) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(label, maxLines = 1)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onThemeSelected(gameTheme.id) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(label, maxLines = 1)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (!adsRemoved) {
            Button(onClick = onRemoveAds, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.remove_ads))
            }
        }
        TextButton(onClick = onRestorePurchases, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.restore_purchases))
        }
        if (privacyOptionsRequired) {
            TextButton(onClick = onPrivacyOptions, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.privacy_options))
            }
        }
        TextButton(onClick = onHowToPlay, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.how_to_play))
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.close))
        }
    }
}

@Composable
fun RemoveAdsDialog(
    price: String?,
    onBuy: () -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
) {
    GameDialog(onDismiss = onDismiss) {
        Text(
            text = stringResource(Res.string.remove_ads_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.remove_ads_desc),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBuy, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (price != null) {
                    stringResource(Res.string.buy_for, price)
                } else {
                    stringResource(Res.string.buy)
                },
            )
        }
        TextButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.restore_purchases))
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.not_now))
        }
    }
}

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    GameDialog(onDismiss = onDismiss) {
        Text(
            text = stringResource(Res.string.how_to_play),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.how_to_play_text),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.close))
        }
    }
}

@Composable
private fun themeLabel(id: String): String = when (id) {
    "DARK" -> stringResource(Res.string.theme_dark)
    "NEON" -> stringResource(Res.string.theme_neon)
    else -> stringResource(Res.string.theme_classic)
}
