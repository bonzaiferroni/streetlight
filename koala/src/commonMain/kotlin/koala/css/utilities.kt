package koala.css

import kotlinx.css.*
import kotlinx.css.properties.Animation
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.Timing
import kotlinx.css.properties.s

// display utilities
object DisplayNone : CssClass { override val value = "display-none" }
object Dim: CssClass { override val value = "dim" }
object NoDim: CssClass { override val value = "no-dim" }
object Glow: CssClass { override val value = "glow" }

// font utilities
object Bold: CssClass { override val value = "bold" }
object Italic: CssClass { override val value = "italic" }
object Large: CssClass { override val value = "large" }

fun CssBuilder.utilities(theme: KoalaTheme) {

    // display utilities
    rule(DisplayNone) {
        display = Display.none
    }

    rule(Dim) {
        color = theme.fg.changeAlpha(0.6)
    }

    rule(NoDim) {
        color = theme.fg
    }

    rule(Glow) {
        animation += Animation("glow", 10.s, iterationCount = IterationCount.infinite, timing = Timing.linear)
    }

    // font utilities

    rule(Bold) {
        fontWeight = FontWeight.bold
    }

    rule(Italic) {
        fontStyle = FontStyle.italic
    }

    "p.${Large.value}" {
        fontSize = 1.4.rem
    }
}