package streetlight.model.data

import koala.css.BackgroundLight
import koala.css.CirclePosition
import koala.css.Koala
import koala.css.Rgb
import kotlinx.serialization.Serializable

@Serializable
data class PageDesign(
    val layout: PageLayout?,
    val theme: PageTheme?,
)

@Serializable
data class PageTheme(
    val accent: Rgb = Koala.accent,
    val primary: Rgb = Koala.primary,
    val rho: BackgroundLight = Koala.rho,
    val beta: BackgroundLight = Koala.beta,
    val gamma: BackgroundLight = Koala.gamma,
)