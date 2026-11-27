package io.github.windowslpg.oplus_toolkit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OplusPrimaryDark,
    onPrimary = OplusOnPrimaryDark,
    primaryContainer = OplusPrimaryContainerDark,
    onPrimaryContainer = OplusOnPrimaryContainerDark,
    secondary = OplusSecondaryDark,
    onSecondary = OplusOnSecondaryDark,
    secondaryContainer = OplusSecondaryContainerDark,
    onSecondaryContainer = OplusOnSecondaryContainerDark,
    tertiary = OplusTertiaryDark,
    onTertiary = OplusOnTertiaryDark,
    tertiaryContainer = OplusTertiaryContainerDark,
    onTertiaryContainer = OplusOnTertiaryContainerDark,
    background = OplusBackgroundDark,
    onBackground = OplusOnBackgroundDark,
    surface = OplusSurfaceDark,
    onSurface = OplusOnSurfaceDark,
    surfaceContainerLow = OplusSurfaceContainerLowDark,
    surfaceContainer = OplusSurfaceContainerDark,
    surfaceVariant = OplusSurfaceVariantDark,
    onSurfaceVariant = OplusOnSurfaceVariantDark,
    outline = OplusOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = OplusPrimaryLight,
    onPrimary = OplusOnPrimaryLight,
    primaryContainer = OplusPrimaryContainerLight,
    onPrimaryContainer = OplusOnPrimaryContainerLight,
    secondary = OplusSecondaryLight,
    onSecondary = OplusOnSecondaryLight,
    secondaryContainer = OplusSecondaryContainerLight,
    onSecondaryContainer = OplusOnSecondaryContainerLight,
    tertiary = OplusTertiaryLight,
    onTertiary = OplusOnTertiaryLight,
    tertiaryContainer = OplusTertiaryContainerLight,
    onTertiaryContainer = OplusOnTertiaryContainerLight,
    background = OplusBackgroundLight,
    onBackground = OplusOnBackgroundLight,
    surface = OplusSurfaceLight,
    onSurface = OplusOnSurfaceLight,
    surfaceContainerLow = OplusSurfaceContainerLowLight,
    surfaceContainer = OplusSurfaceContainerLight,
    surfaceVariant = OplusSurfaceVariantLight,
    onSurfaceVariant = OplusOnSurfaceVariantLight,
    outline = OplusOutlineLight
)

@Composable
fun OplusToolkitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
