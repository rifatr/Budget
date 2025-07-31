package com.example.budget.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DeepPurple,
    onPrimary = Color.White,
    primaryContainer = MidnightBlue,
    onPrimaryContainer = Color.White,
    secondary = Rose,
    onSecondary = Color.White,
    secondaryContainer = Cherry,
    onSecondaryContainer = Color.White,
    tertiary = Teal,
    onTertiary = Color.White,
    tertiaryContainer = Aqua,
    onTertiaryContainer = Color.Black,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2B32),
    onSurfaceVariant = Color(0xFFE1E2E9),
    outline = Color(0xFF8B909A),
    error = Color(0xFFFF6B6B),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OceanBlue,
    onPrimary = Color.White,
    primaryContainer = SkyBlue,
    onPrimaryContainer = Color.White,
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = Peach,
    onSecondaryContainer = Color.White,
    tertiary = Mint,
    onTertiary = Color.White,
    tertiaryContainer = Lavender,
    onTertiaryContainer = Color.Black,
    background = LightBackground,
    onBackground = Color(0xFF1A1B23),
    surface = LightSurface,
    onSurface = Color(0xFF1A1B23),
    surfaceVariant = Color(0xFFE8EAED),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
    error = Color(0xFFDC3545),
    onError = Color.White
)

@Composable
fun BudgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our beautiful gradient theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val gradientColors = if (isDark) {
        listOf(
            DarkBackground,
            Color(0xFF1E2028),
            Color(0xFF252832)
        )
    } else {
        listOf(
            GradientStart,
            GradientMiddle,
            GradientEnd
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        content()
    }
} 