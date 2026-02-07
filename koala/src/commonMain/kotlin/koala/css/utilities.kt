package koala.css

import kotlinx.css.*
import kotlinx.css.properties.Animation
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.Timing
import kotlinx.css.properties.s

// display utilities
object DisplayNone : CssClass { override val value = "display-none" }
object Opacity6: CssClass { override val value = "opacity-6" }
object Opacity4: CssClass { override val value = "opacity-4" }
object Opacity2: CssClass { override val value = "opacity-2" }
object Dim: CssClass { override val value = Opacity6.value }
object NoDim: CssClass { override val value = "no-dim" }
object Glow: CssClass { override val value = "glow" }
object CircleShape: CssClass { override val value = "circle-shape" }

// font utilities
object Bold: CssClass { override val value = "bold" }
object Italic: CssClass { override val value = "italic" }
object Large: CssClass { override val value = "large" }

fun CssBuilder.utilities(theme: KoalaTheme) {

    // display utilities
    rule(DisplayNone) {
        display = Display.none
    }

    rule(Opacity6) {
        color = theme.fg.changeAlpha(0.6)
    }

    rule(Opacity4) {
        color = theme.fg.changeAlpha(0.4)
    }

    rule(Opacity2) {
        color = theme.fg.changeAlpha(0.2)
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