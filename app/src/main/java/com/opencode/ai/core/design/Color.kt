package com.opencode.ai.core.design

import androidx.compose.ui.graphics.Color

/**
 * OpenCode color palette faithfully ported from the original Go TUI themes
 * (opencode, catppuccin, dracula, monokai, onedark, tokyonight, flexoki, tron).
 */
data class OpenCodeColors(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val backgroundSecondary: Color,
    val backgroundDarker: Color,
    val surface: Color,
    val text: Color,
    val textMuted: Color,
    val textEmphasized: Color,
    val borderNormal: Color,
    val borderFocused: Color,
    val borderDim: Color,
    val error: Color,
    val warning: Color,
    val success: Color,
    val info: Color,
    val diffAdded: Color,
    val diffRemoved: Color,
    val diffAddedBg: Color,
    val diffRemovedBg: Color,
    val diffLineNumber: Color,
    val codeBlockBg: Color,
    val isDark: Boolean
)

object OpenCodePalettes {
    // Original OpenCode Dark Theme
    val Dark = OpenCodeColors(
        primary = Color(0xFFFAB283),      // Primary orange/gold
        secondary = Color(0xFF5C9CF5),    // Secondary blue
        accent = Color(0xFF9D7CD8),       // Accent purple
        background = Color(0xFF212121),   // Main dark background
        backgroundSecondary = Color(0xFF252525),
        backgroundDarker = Color(0xFF141414),
        surface = Color(0xFF2A2A2A),
        text = Color(0xFFE0E0E0),
        textMuted = Color(0xFF8A8A8A),
        textEmphasized = Color(0xFFE5C07B),
        borderNormal = Color(0xFF4B4C5C),
        borderFocused = Color(0xFFFAB283),
        borderDim = Color(0xFF333338),
        error = Color(0xFFE06C75),
        warning = Color(0xFFF5A742),
        success = Color(0xFF7FD88F),
        info = Color(0xFF56B6C2),
        diffAdded = Color(0xFF478247),
        diffRemoved = Color(0xFFC62828),
        diffAddedBg = Color(0xFF1B2E1B),
        diffRemovedBg = Color(0xFF351B1B),
        diffLineNumber = Color(0xFF757575),
        codeBlockBg = Color(0xFF181818),
        isDark = true
    )

    // Original OpenCode Light Theme
    val Light = OpenCodeColors(
        primary = Color(0xFF3B7DD8),
        secondary = Color(0xFF7B5BB6),
        accent = Color(0xFFD68C27),
        background = Color(0xFFF8F8F8),
        backgroundSecondary = Color(0xFFF0F0F0),
        backgroundDarker = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        text = Color(0xFF2A2A2A),
        textMuted = Color(0xFF757575),
        textEmphasized = Color(0xFFB0851F),
        borderNormal = Color(0xFFD3D3D3),
        borderFocused = Color(0xFF3B7DD8),
        borderDim = Color(0xFFE5E5E6),
        error = Color(0xFFD1383D),
        warning = Color(0xFFD68C27),
        success = Color(0xFF3D9A57),
        info = Color(0xFF318795),
        diffAdded = Color(0xFF2E7D32),
        diffRemoved = Color(0xFFC62828),
        diffAddedBg = Color(0xFFE8F5E9),
        diffRemovedBg = Color(0xFFFFEBEE),
        diffLineNumber = Color(0xFF9E9E9E),
        codeBlockBg = Color(0xFFEAEAEA),
        isDark = false
    )

    // Dracula Theme from upstream OpenCode
    val Dracula = OpenCodeColors(
        primary = Color(0xFFFF79C6),
        secondary = Color(0xFFBD93F9),
        accent = Color(0xFF8BE9FD),
        background = Color(0xFF282A36),
        backgroundSecondary = Color(0xFF343746),
        backgroundDarker = Color(0xFF1E1F29),
        surface = Color(0xFF44475A),
        text = Color(0xFFF8F8F2),
        textMuted = Color(0xFF6272A4),
        textEmphasized = Color(0xFFF1FA8C),
        borderNormal = Color(0xFF6272A4),
        borderFocused = Color(0xFFFF79C6),
        borderDim = Color(0xFF3E4153),
        error = Color(0xFFFF5555),
        warning = Color(0xFFFFB86C),
        success = Color(0xFF50FA7B),
        info = Color(0xFF8BE9FD),
        diffAdded = Color(0xFF50FA7B),
        diffRemoved = Color(0xFFFF5555),
        diffAddedBg = Color(0xFF1C3A24),
        diffRemovedBg = Color(0xFF3D1E22),
        diffLineNumber = Color(0xFF6272A4),
        codeBlockBg = Color(0xFF1E1F29),
        isDark = true
    )

    // Tokyo Night Theme from upstream OpenCode
    val TokyoNight = OpenCodeColors(
        primary = Color(0xFF7AA2F7),
        secondary = Color(0xFFBB9AF7),
        accent = Color(0xFF7DCFFF),
        background = Color(0xFF1A1B26),
        backgroundSecondary = Color(0xFF24283B),
        backgroundDarker = Color(0xFF16161E),
        surface = Color(0xFF292E42),
        text = Color(0xFFC0CAF5),
        textMuted = Color(0xFF565F89),
        textEmphasized = Color(0xFFE0AF68),
        borderNormal = Color(0xFF3B4261),
        borderFocused = Color(0xFF7AA2F7),
        borderDim = Color(0xFF23283E),
        error = Color(0xFFF7768E),
        warning = Color(0xFFE0AF68),
        success = Color(0xFF9ECE6A),
        info = Color(0xFF7DCFFF),
        diffAdded = Color(0xFF9ECE6A),
        diffRemoved = Color(0xFFF7768E),
        diffAddedBg = Color(0xFF1C2C24),
        diffRemovedBg = Color(0xFF381F28),
        diffLineNumber = Color(0xFF565F89),
        codeBlockBg = Color(0xFF16161E),
        isDark = true
    )
}
