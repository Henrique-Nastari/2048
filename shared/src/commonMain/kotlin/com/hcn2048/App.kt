package com.hcn2048

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.hcn2048.ads.AdManager
import com.hcn2048.ui.GameViewModel
import com.hcn2048.ui.screens.GameScreen
import com.hcn2048.ui.theme.GameTheme
import org.koin.core.context.GlobalContext

/**
 * Root composable shared by Android (and iOS in the future).
 * DI is resolved from the global Koin context started by the platform entry point.
 */
@Composable
fun App() {
    val viewModel: GameViewModel = remember { GlobalContext.get().get() }
    val ads: AdManager = remember { GlobalContext.get().get() }
    val state by viewModel.uiState.collectAsState()
    val theme = GameTheme.byId(state.themeId)

    MaterialTheme(
        colorScheme = if (theme.isDark) darkColorScheme() else lightColorScheme(),
    ) {
        GameScreen(viewModel = viewModel, ads = ads, state = state)
    }
}
