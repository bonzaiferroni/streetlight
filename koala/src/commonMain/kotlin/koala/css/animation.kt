package koala.css

import kotlinx.css.*
import kotlinx.css.properties.*

@Deprecated("use reveal")
object Show: CssClass { override val value = "show" }
object FadeStack: CssClass { override val value = "fade-stack" }

// utilities
object Animate: CssClass { override val value = "animate" }
object Reveal: CssClass { override val value = "reveal" }
// object Hide: CssClass { override val value = "hide" }
object Blur: CssClass { override val value = "blur" }
object SlideX: CssClass { override val value = "slide-x" }
object SlideY: CssClass { override val value = "slide-y" }

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