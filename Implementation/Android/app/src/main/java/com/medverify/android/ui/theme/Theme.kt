package com.medverify.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MedVerifyBlue,
    secondary = MedVerifyBlueDark,
    background = MedVerifyBackground,
)

private val DarkColors = darkColorScheme(
    primary = MedVerifyBlue,
    secondary = MedVerifyBlueDark,
)

@Composable
fun MedVerifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
