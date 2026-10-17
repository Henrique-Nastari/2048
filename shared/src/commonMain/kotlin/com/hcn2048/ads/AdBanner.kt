package com.hcn2048.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform ad banner. Renders nothing when ads are removed, not yet available
 * or on platforms without a banner implementation.
 */
@Composable
expect fun AdBanner(modifier: Modifier = Modifier)
