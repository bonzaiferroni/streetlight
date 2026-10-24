package streetlight.compose.view

import androidx.compose.runtime.Immutable

/** The theme's opacities, the web's `--opacity-low`, `--opacity-half` and `--opacity-high`. */
@Immutable
class KoalaOpacity(
    val low: Float = 0.25f,
    val half: Float = 0.5f,
    val high: Float = 0.85f,
)
