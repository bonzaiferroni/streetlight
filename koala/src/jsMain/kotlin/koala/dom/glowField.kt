package koala.dom

import koala.modifier.Css
import koala.modifier.Glow
import koala.modifier.ModifierSet
import koala.modifier.modify
import koala.modifier.rgba
import kampfire.model.MutableTap
import koala.model.GlowControlStyle
import koala.modifier.setStyle
import kotlinx.css.pct
import web.events.addEventListener
import web.html.HTMLElement
import web.pointer.POINTER_DOWN
import web.pointer.POINTER_MOVE
import web.pointer.PointerEvent
import kotlin.math.roundToInt

fun ViewScope.glowField(
    state: MutableTap<Glow>,
    mod: ModifierSet? = null,
) {
    lateinit var fieldElement: HTMLElement
    lateinit var handleElement: HTMLElement

    fun place(glow: Glow) {
        handleElement.setStyle(Css.Left.of(glow.position.x.pct))
        handleElement.setStyle(Css.Top.of(glow.position.y.pct))
        handleElement.setStyle(Css.Width.of((glow.position.radius / 2.0).pct))
        handleElement.setStyle(Css.BackgroundColor.of(glow.rgba()))
    }

    fun report(event: PointerEvent) {
        val rect = fieldElement.getBoundingClientRect()
        if (rect.width == 0.0 || rect.height == 0.0) return
        state.set {
            copy(
                position = position.copy(
                    x = ((event.clientX - rect.left) / rect.width * 100).roundToInt().coerceIn(0, 100),
                    y = ((event.clientY - rect.top) / rect.height * 100).roundToInt().coerceIn(0, 100),
                )
            )
        }
    }

    fieldElement = box(modify(mod, GlowControlStyle.Field)) {
        handleElement = box(modify(GlowControlStyle.Handle))
    }

    handleElement.addEventListener(PointerEvent.POINTER_DOWN, { event ->
        handleElement.setPointerCapture(event.pointerId)
    })

    handleElement.addEventListener(PointerEvent.POINTER_MOVE, { event ->
        if (handleElement.hasPointerCapture(event.pointerId)) report(event)
    })

    place(state.now)

    launchEffect(::glowField) {
        state.flow.collect { place(it) }
    }
}