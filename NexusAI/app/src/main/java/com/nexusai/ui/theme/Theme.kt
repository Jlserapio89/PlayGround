package com.nexusai.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NexusAccent = Color(0xFF6C4DFF)
private val NexusTeal = Color(0xFF00D1B2)

private val DarkScheme = darkColorScheme(primary = NexusAccent, secondary = NexusTeal, tertiary = Color(0xFF9D86FF))
private val LightScheme = lightColorScheme(primary = Color(0xFF5A3DF0), secondary = Color(0xFF009E86), tertiary = Color(0xFF7A5CFF))

@Composable
fun NexusTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
}
