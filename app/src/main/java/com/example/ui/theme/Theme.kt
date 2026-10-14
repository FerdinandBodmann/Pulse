package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PulseVioletLight,
    onPrimary = Color(0xFF0F0E2A),
    primaryContainer = PulseVioletDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = PulseCyanLight,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = PulseCyanDark,
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = PulseCoralLight,
    onTertiary = Color(0xFF4C0519),
    tertiaryContainer = PulseCoralDark,
    onTertiaryContainer = Color(0xFFFFE4E6),
    background = PulseDarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = PulseDarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = PulseDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = PulseDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = PulseVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = PulseCyanDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECFEFF),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = PulseCoralAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4E6),
    onTertiaryContainer = Color(0xFF881337),
    background = PulseLightBg,
    onBackground = Color(0xFF0F172A),
    surface = PulseLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = PulseLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = PulseLightOutline
)

@Composable
fun PulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our brand colors by default for distinctive aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
