package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import koala.css.modify
import koala.html.Attribute
import koala.html.Id
import koala.html.Queryable
import koala.html.SwitchStyle
import koala.html.setAttribute
import koala.html.setId
import koala.model.MutableTap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.span
import web.events.addEventListener
import web.html.HTMLDivElement
import web.keyboard.KEY_DOWN
import web.keyboard.KeyboardEvent

fun ViewScope.switch(
    label: String,
    state: MutableTap<Boolean>,
    mod: ModifierSet? = null,
    id: Id? = null,
    block: (DIV.() -> Unit)? = null,
): HTMLDivElement {
    var currentValue = state.now

    val element = div {
        addModifiers(modify(SwitchStyle.Class, mod))
        setId(id)
        attributes["role"] = "switch"
        attributes["aria-checked"] = currentValue.toString()
        setAttribute(Attribute.IsOn, currentValue)

        // ghost text defines the inner pill width; outer padding makes the "constraints" larger
        span(label, modify(SwitchStyle.Ghost))
        span(label, modify(SwitchStyle.Pill))

        block?.invoke(this)
    }.asWeb()

    fun display(value: Boolean) {
        if (value == currentValue) return
        currentValue = value
        element.setAttribute(Attribute.IsOn.to(value))
        element.setAttribute("aria-checked", value.toString())
    }

    element.onClick { state.set(!currentValue) }
    element.addEventListener(KeyboardEvent.KEY_DOWN, { event ->
        val key = event.key
        if (key == "Enter" || key == " ") {
            event.preventDefault()
            state.set(!currentValue)
        }
    })

    launchEffect(::switch) {
        state.flow.collect { display(it) }
    }

    return element
}