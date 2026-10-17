package com.hcn2048.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hcn2048.resources.Res
import com.hcn2048.resources.best
import com.hcn2048.resources.score
import com.hcn2048.ui.theme.GameTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScoreHeader(
    score: Int,
    best: Int,
    theme: GameTheme,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "2048",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = theme.textPrimary,
            modifier = Modifier.weight(1f),
        )
        ScoreCard(label = stringResource(Res.string.score), value = score, theme = theme)
        ScoreCard(label = stringResource(Res.string.best), value = best, theme = theme)
    }
}

@Composable
private fun ScoreCard(label: String, value: Int, theme: GameTheme) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(theme.scoreCardBackground)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = theme.scoreCardLabel,
            letterSpacing = 1.sp,
        )
        Text(
            text = value.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.scoreCardValue,
        )
    }
}
