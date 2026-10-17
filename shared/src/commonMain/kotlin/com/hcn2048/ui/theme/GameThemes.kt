package com.hcn2048.ui.theme

import androidx.compose.ui.graphics.Color

/** Colors of a single tile value. */
data class TileColors(val background: Color, val text: Color)

/** Visual identity of the game. Players can switch themes from Settings. */
data class GameTheme(
    val id: String,
    val isDark: Boolean,
    val background: Color,
    val boardBackground: Color,
    val emptyCell: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val scoreCardBackground: Color,
    val scoreCardLabel: Color,
    val scoreCardValue: Color,
    val tileColors: Map<Int, TileColors>,
) {
    /** Falls back to the 8192 palette for values beyond the map. */
    fun colorsForTile(value: Int): TileColors =
        tileColors[value] ?: tileColors.getValue(8192)

    companion object {
        val CLASSIC = GameTheme(
            id = "CLASSIC",
            isDark = false,
            background = Color(0xFFFAF8EF),
            boardBackground = Color(0xFFBBADA0),
            emptyCell = Color(0xFFCDC1B4),
            textPrimary = Color(0xFF776E65),
            textSecondary = Color(0xFF9C8F84),
            scoreCardBackground = Color(0xFFBBADA0),
            scoreCardLabel = Color(0xFFEEE4DA),
            scoreCardValue = Color(0xFFFFFFFF),
            tileColors = mapOf(
                2 to tile(0xFFEEE4DA, 0xFF776E65),
                4 to tile(0xFFEDE0C8, 0xFF776E65),
                8 to tile(0xFFF2B179, 0xFFF9F6F2),
                16 to tile(0xFFF59563, 0xFFF9F6F2),
                32 to tile(0xFFF67C5F, 0xFFF9F6F2),
                64 to tile(0xFFF65E3B, 0xFFF9F6F2),
                128 to tile(0xFFEDCF72, 0xFFF9F6F2),
                256 to tile(0xFFEDCC61, 0xFFF9F6F2),
                512 to tile(0xFFEDC850, 0xFFF9F6F2),
                1024 to tile(0xFFEDC53F, 0xFFF9F6F2),
                2048 to tile(0xFFEDC22E, 0xFFF9F6F2),
                4096 to tile(0xFF3C3A32, 0xFFF9F6F2),
                8192 to tile(0xFF3C3A32, 0xFFF9F6F2),
            ),
        )

        val DARK = GameTheme(
            id = "DARK",
            isDark = true,
            background = Color(0xFF16161A),
            boardBackground = Color(0xFF26262E),
            emptyCell = Color(0xFF32323C),
            textPrimary = Color(0xFFECECF1),
            textSecondary = Color(0xFF9A9AA5),
            scoreCardBackground = Color(0xFF26262E),
            scoreCardLabel = Color(0xFFA8A8B3),
            scoreCardValue = Color(0xFFFFFFFF),
            tileColors = mapOf(
                2 to tile(0xFF3B3B43, 0xFFECECF1),
                4 to tile(0xFF4A4A52, 0xFFECECF1),
                8 to tile(0xFFAD7A5E, 0xFFFFFFFF),
                16 to tile(0xFFB9704C, 0xFFFFFFFF),
                32 to tile(0xFFC25F3E, 0xFFFFFFFF),
                64 to tile(0xFFC74B2C, 0xFFFFFFFF),
                128 to tile(0xFFC9A24B, 0xFFFFFFFF),
                256 to tile(0xFFCBA343, 0xFFFFFFFF),
                512 to tile(0xFFCCA137, 0xFFFFFFFF),
                1024 to tile(0xFFCE9E2E, 0xFFFFFFFF),
                2048 to tile(0xFFC99C24, 0xFFFFFFFF),
                4096 to tile(0xFF2E2E36, 0xFFFFFFFF),
                8192 to tile(0xFF2E2E36, 0xFFFFFFFF),
            ),
        )

        val NEON = GameTheme(
            id = "NEON",
            isDark = true,
            background = Color(0xFF07070F),
            boardBackground = Color(0xFF101024),
            emptyCell = Color(0xFF1B1B33),
            textPrimary = Color(0xFF7DF9FF),
            textSecondary = Color(0xFF57A9C0),
            scoreCardBackground = Color(0xFF14142B),
            scoreCardLabel = Color(0xFF7DF9FF),
            scoreCardValue = Color(0xFFFFFFFF),
            tileColors = mapOf(
                2 to tile(0xFF7DF9FF, 0xFF071018),
                4 to tile(0xFF64E9FB, 0xFF071018),
                8 to tile(0xFF45D4F0, 0xFF071018),
                16 to tile(0xFF23B8DE, 0xFF051117),
                32 to tile(0xFF0E9CC4, 0xFFEAFDFF),
                64 to tile(0xFF0B7FA6, 0xFFEAFDFF),
                128 to tile(0xFF08618C, 0xFFEAFDFF),
                256 to tile(0xFF0A4E74, 0xFFEAFDFF),
                512 to tile(0xFF0C3B5C, 0xFFEAFDFF),
                1024 to tile(0xFF39E0FF, 0xFF071018),
                2048 to tile(0xFFFF4FD8, 0xFF071018),
                4096 to tile(0xFFA24BFF, 0xFFFFFFFF),
                8192 to tile(0xFF7A2FF2, 0xFFFFFFFF),
            ),
        )

        val ALL = listOf(CLASSIC, DARK, NEON)

        fun byId(id: String): GameTheme = ALL.firstOrNull { it.id == id } ?: CLASSIC
    }
}

private fun tile(background: Long, text: Long) =
    TileColors(Color(background), Color(text))
