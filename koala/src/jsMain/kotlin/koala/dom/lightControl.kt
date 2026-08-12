package koala.dom

import koala.css.BackgroundLight
import koala.css.ModifierSet
import koala.css.Property
import koala.css.modify
import koala.css.toHex
import koala.model.MutableField
import koala.model.LightControl
import kotlinx.browser.window
import kotlinx.css.pct
import org.w3c.dom.HTMLElement
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.math.roundToInt

fun ViewScope.lightControl(
    lightState: MutableField<BackgroundLight>,
    mod: ModifierSet? = null,
) {
    lateinit var fieldElement: HTMLElement
    lateinit var handleElement: HTMLElement

    fun place(light: BackgroundLight) {
        handleElement.setStyle(Property.Left.to(light.position.x.pct))
        handleElement.setStyle(Property.Top.to(light.position.y.pct))
        handleElement.setStyle(Property.BackgroundColor.to(light.color.toHex()))
        light.position.radius.let {
            handleElement.setStyle(Property.Width.to((it / 4).pct))
        }
    }

    fun report(event: PointerEvent) {
        val rect = fieldElement.getBoundingClientRect()
        if (rect.width == 0.0 || rect.height == 0.0) return
        lightState.set {
            copy(
                position = position.copy(
                    x = ((event.clientX - rect.left) / rect.width * 100).roundToInt().coerceIn(0, 100),
                    y = ((event.clientY - rect.top) / rect.height * 100).roundToInt().coerceIn(0, 100),
                )
            )
        }
    }

    fieldElement = box(modify(mod, LightControl.Field)) {
        handleElement = box(modify(LightControl.Handle))
    }

    handleElement.addEventListener("pointerdown", { event ->
        event as PointerEvent
        handleElement.setPointerCapture(event.pointerId)
    })

    handleElement.addEventListener("pointermove", { event ->
        event as PointerEvent
        if (handleElement.hasPointerCapture(event.pointerId)) report(event)
    })

    place(lightState.now)

    launchEffect(::lightControl) {
        lightState.flow.collect { place(it) }
    }
}