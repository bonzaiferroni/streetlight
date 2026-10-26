package streetlight.model.data

import koala.modifier.Glow
import koala.modifier.Koala
import koala.modifier.Rgb
import kampfire.model.Labeled
import kotlinx.serialization.Serializable

/** The layout and theme of a page. */
@Serializable
data class PageDesign(
    val layout: PageLayout?,
    val theme: PageTheme?,
)

/** The colors and background glows of a page. */
@Serializable
data class PageTheme(
    val accent: Rgb = Koala.accent,
    val primary: Rgb = Koala.primary,
    val rho: Glow = Koala.rho,
    val beta: Glow = Koala.beta,
    val gamma: Glow = Koala.gamma,
    val backgroundMode: PageBackgroundMode = PageBackgroundMode.Flux,
)

/** The way a page shows its background lights. */
enum class PageBackgroundMode: Labeled {
    /** No lights. */
    None,

    /** Lights that hold their colors. */
    Static,

    /** Lights whose colors slowly drift. */
    Flux;

    override val label get() = name
}