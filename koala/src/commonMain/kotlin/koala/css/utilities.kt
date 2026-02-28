package koala.css

import kotlinx.css.*
import kotlinx.css.properties.Animation
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.Timing
import kotlinx.css.properties.s

// display utilities
object DisplayNone : Modifier { override val value = "display-none" }
object Opacity6: Modifier { override val value = "opacity-6" }
object Opacity4: Modifier { override val value = "opacity-4" }
object Opacity2: Modifier { override val value = "opacity-2" }
object Dim: Modifier { override val value = Opacity6.value }
object NoDim: Modifier { override val value = "no-dim" }
object Glow: Modifier { override val value = "glow" }
object GlowShadow: Modifier { override val value = "glow-shadow" }
object GlowBackground: Modifier { override val value = "glow-background" }
object CircleShape: Modifier { override val value = "circle-shape" }
object BorderRadius1: Modifier { override val value = "border-radius-1" }
object Accent: Modifier { override val value = "accent" }
object Primary: Modifier { override val value = "primary" }
object Secondary: Modifier { override val value = "secondary" }
object Clickable: Modifier { override val value = "clickable" }

// font utilities
object Bold: Modifier { override val value = "bold" }
object Italic: Modifier { override val value = "italic" }
object Large: Modifier { override val value = "large" }
object Heading1: Modifier { override val value = "heading-1" }
object Heading2: Modifier { override val value = "heading-2" }

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

//    rule(GlowShadow) {
//        animation += Animation("glow-shadow", 10.s, iterationCount = IterationCount.infinite, timing = Timing.linear)
//    }

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