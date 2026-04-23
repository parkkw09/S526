package app.peter.s526.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Illuminating40,
    onPrimary = OnIlluminating,
    primaryContainer = Illuminating80,
    onPrimaryContainer = OnIlluminating,
    secondary = Teal40,
    onSecondary = OnTeal,
    secondaryContainer = Teal80,
    onSecondaryContainer = OnTeal,
    background = Gray99,
    onBackground = Gray10,
    surface = SurfaceLight,
    onSurface = Gray10,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Gray20,
    error = ErrorLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = Illuminating,
    onPrimary = OnIlluminating,
    primaryContainer = Illuminating40,
    onPrimaryContainer = OnIlluminating,
    secondary = Teal80,
    onSecondary = OnTeal,
    secondaryContainer = Teal40,
    onSecondaryContainer = Teal80,
    background = Gray10,
    onBackground = Gray90,
    surface = SurfaceDark,
    onSurface = Gray90,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Gray80,
    error = ErrorDark,
)

/**
 * S526 앱의 Material 3 테마.
 * Android 12+ 에서는 Dynamic Color 를 자동 적용하고,
 * 그 이하에서는 커스텀 Illuminating/Teal 컬러 스킴을 사용한다.
 */
@Composable
fun S526Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
        typography = S526Typography,
        content = content,
    )
}
