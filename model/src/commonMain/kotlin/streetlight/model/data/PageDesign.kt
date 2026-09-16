package streetlight.model.data

import koala.modifier.Glow
import koala.modifier.Koala
import koala.modifier.Rgb
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
    val rho: Glow = Koala.rho,
    val beta: Glow = Koala.beta,
    val gamma: Glow = Koala.gamma,
    val colorFlux: Boolean = true,
)