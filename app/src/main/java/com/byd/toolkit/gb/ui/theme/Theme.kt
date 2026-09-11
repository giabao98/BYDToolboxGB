package com.byd.toolkit.gb.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CockpitColorScheme = darkColorScheme(
    primary = CyanPrimary,
    secondary = CyanSecondary,
    tertiary = AmberWarning,
    background = CockpitBg,
    surface = CockpitSurface,
    onPrimary = TextDark,
    onSecondary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = CrimsonDanger
)

@Composable
fun BYDToolkitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CockpitColorScheme,
        content = content
    )
}
