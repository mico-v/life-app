package com.example.android16demo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.example.android16demo.data.sync.SyncPreferences

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Local composition for theme mode
 */
val LocalThemeMode = compositionLocalOf { SyncPreferences.THEME_SYSTEM }

/**
 * Local composition for language
 */
val LocalLanguage = compositionLocalOf { SyncPreferences.LANGUAGE_SYSTEM }

@Composable
fun Android16DemoTheme(
    themeMode: String = SyncPreferences.THEME_SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        SyncPreferences.THEME_DARK -> true
        SyncPreferences.THEME_LIGHT -> false
        else -> isSystemInDarkTheme()
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
