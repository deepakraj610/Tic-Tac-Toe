package com.example.tictactoe.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Accent colors for the two marks, kept distinct from the Material color roles. */
@Immutable
data class GameColors(
    val markX: Color,
    val markO: Color,
)

private val LightGameColors = GameColors(markX = XAccentLight, markO = OAccentLight)
private val DarkGameColors = GameColors(markX = XAccentDark, markO = OAccentDark)

val LocalGameColors = staticCompositionLocalOf { LightGameColors }

/** Access the X/O accent colors: `MaterialTheme.gameColors.markX`. */
val MaterialTheme.gameColors: GameColors
    @Composable
    @ReadOnlyComposable
    get() = LocalGameColors.current

private val LightColorScheme =
    lightColorScheme(
        primary = Indigo40,
        secondary = IndigoGrey40,
        tertiary = Rose40,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Indigo80,
        secondary = IndigoGrey80,
        tertiary = Rose80,
    )

@Composable
fun TicTacToeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    val gameColors = if (darkTheme) DarkGameColors else LightGameColors

    CompositionLocalProvider(LocalGameColors provides gameColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
