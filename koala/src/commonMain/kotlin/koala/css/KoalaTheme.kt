package koala.css

import kotlinx.css.*

data class KoalaTheme(
    val spacingUnit: LinearDimension = 0.5.rem,
    val fg: Color = rgb(245, 246, 246),
    val void: Color = rgb(24, 31, 31)
)