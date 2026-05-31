package com.example.jetpackstayrooms.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = RustOrange,
    onPrimary = Color.White,
    primaryContainer = RustOrange.copy(alpha = 0.2f),
    onPrimaryContainer = DeepNavy,
    secondary = LightSage,
    onSecondary = Color.White,
    secondaryContainer = LightSage.copy(alpha = 0.3f),
    onSecondaryContainer = DeepNavy,
    tertiary = AccentGold,
    onTertiary = Color.White,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed,
    background = SoftCream,
    onBackground = DeepNavy,
    surface = Color.White,
    onSurface = DeepNavy,
    surfaceVariant = SoftCream,
    onSurfaceVariant = DeepNavy.copy(alpha = 0.7f),
    outline = WarmTaupe.copy(alpha = 0.5f)
)

/**
 * Tema raíz Material 3 de la aplicación.
 *
 * Aplica la paleta cálida personalizada (definida en `Color.kt`) y la
 * tipografía mixta Serif/Sans-Serif (`Type.kt`). No expone variante oscura: el
 * diseño se ha definido únicamente en claro.
 */
@Composable
fun JetPackStayRoomsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}