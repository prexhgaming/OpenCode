package com.opencode.ai.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalOpenCodeColors = staticCompositionLocalOf { OpenCodePalettes.Dark }

object OpenCodeTheme {
    val colors: OpenCodeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalOpenCodeColors.current
}

@Composable
fun OpenCodeAppTheme(
    themeName: String = "opencode",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val openCodeColors = when (themeName.lowercase()) {
        "dracula" -> OpenCodePalettes.Dracula
        "tokyonight" -> OpenCodePalettes.TokyoNight
        "opencode_light", "light" -> OpenCodePalettes.Light
        else -> if (darkTheme) OpenCodePalettes.Dark else OpenCodePalettes.Light
    }

    val materialColorScheme = if (openCodeColors.isDark) {
        darkColorScheme(
            primary = openCodeColors.primary,
            secondary = openCodeColors.secondary,
            tertiary = openCodeColors.accent,
            background = openCodeColors.background,
            surface = openCodeColors.surface,
            onPrimary = openCodeColors.backgroundDarker,
            onSecondary = openCodeColors.backgroundDarker,
            onBackground = openCodeColors.text,
            onSurface = openCodeColors.text
        )
    } else {
        lightColorScheme(
            primary = openCodeColors.primary,
            secondary = openCodeColors.secondary,
            tertiary = openCodeColors.accent,
            background = openCodeColors.background,
            surface = openCodeColors.surface,
            onPrimary = openCodeColors.backgroundDarker,
            onSecondary = openCodeColors.backgroundDarker,
            onBackground = openCodeColors.text,
            onSurface = openCodeColors.text
        )
    }

    CompositionLocalProvider(
        LocalOpenCodeColors provides openCodeColors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = OpenCodeTypography,
            shapes = OpenCodeShapes,
            content = content
        )
    }
}
