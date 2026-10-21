package malo.mrn.tunetalk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TunePurple,
    onPrimary = TuneBackground,
    secondary = TuneMint,
    tertiary = TuneCoral,
    background = TuneBackground,
    onBackground = TuneText,
    surface = TuneSurface,
    onSurface = TuneText,
    surfaceVariant = TuneSurfaceVariant,
    onSurfaceVariant = TuneMuted
)

@Composable
fun TunetalkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}