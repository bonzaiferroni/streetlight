package koala.css

import kotlinx.css.*
import kotlinx.css.properties.*

@Deprecated("use reveal")
object Show: Modifier { override val value = "show" }
object FadeStack: Modifier { override val value = "fade-stack" }

// utilities
object Animate: Modifier { override val value = "magic" }
object Reveal: Modifier { override val value = "reveal" }
// object Hide: CssClass { override val value = "hide" }
object MagicBlur: Modifier { override val value = "magic-blur" }
object MagicSlideX: Modifier { override val value = "magic-slide-x" }
object MagicSlideY: Modifier { override val value = "magic-slide-y" }

fun CssBuilder.animation(theme: KoalaTheme) {
    rule(FadeStack) {
        position = Position.relative

        children {
            position = Position.absolute
            inset = Inset(LinearDimension.none)
            opacity = 0
            visibility = Visibility.hidden
            pointerEvents = PointerEvents.none
            transform.scale(0.9)
            transition += Transition("opacity", 0.35.s, Timing.ease)
            transition += Transition("transform", 0.35.s, Timing.ease)
            transition += Transition("visibility", 0.35.s, Timing.ease)
        }

        +rule(Show.value) {
            opacity = 1
            transform.scale(1)
            visibility = Visibility.visible
            pointerEvents = PointerEvents.auto
        }
    }
}