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
    primary = SoftIndigo,
    onPrimary = Color.White,
    primaryContainer = DeepTwilight,
    onPrimaryContainer = MistBlue,
    secondary = LightSlate,
    onSecondary = Color.White,
    secondaryContainer = NightSky,
    onSecondaryContainer = CloudBlue,
    tertiary = SoftTeal,
    onTertiary = Color.White,
    tertiaryContainer = MintGreen,
    onTertiaryContainer = Color.Black,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF394A5A),
    onSurfaceVariant = Color(0xFFE8F0F8),
    outline = Color(0xFF95A5B5),
    error = Color(0xFFFF7A7A),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SerenityBlue,
    onPrimary = Color.White,
    primaryContainer = MistBlue,
    onPrimaryContainer = DeepTwilight,
    secondary = TranquillBlue,
    onSecondary = Color.White,
    secondaryContainer = CloudBlue,
    onSecondaryContainer = DeepTwilight,
    tertiary = SoftTeal,
    onTertiary = Color.White,
    tertiaryContainer = MintGreen,
    onTertiaryContainer = DeepTwilight,
    background = LightBackground,
    onBackground = Color(0xFF2C3E50),
    surface = LightSurface,
    onSurface = Color(0xFF2C3E50),
    surfaceVariant = Color(0xFFF0F6FF),
    onSurfaceVariant = Color(0xFF4A5568),
    outline = Color(0xFF7A8A9A),
    error = Color(0xFFE74C3C),
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
            Color(0xFF2F3F4F),
            Color(0xFF384A5A)
        )
    } else {
        listOf(
            GradientStart,    // Soft sky blue
            GradientMiddle,   // Calm blue  
            GradientEnd       // Almost white
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