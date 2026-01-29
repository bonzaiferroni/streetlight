package koala.css

import kotlinx.css.*
import kotlinx.css.properties.*

fun CssBuilder.animation(theme: KoalaTheme) {
    classRule(FadeStack) {
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