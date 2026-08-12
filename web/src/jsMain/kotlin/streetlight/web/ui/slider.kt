package streetlight.web.ui

import koala.Svg
import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.css.Height3
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.icon
import koala.dom.row
import koala.model.MutableTap
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.js.input
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement

fun ViewScope.slider(
    state: MutableTap<Int>,
    range: IntRange = (0..100),
    step: Int = range.step,
    mod: ModifierSet? = null,
    config: INPUT.() -> Unit = { }
): HTMLInputElement {
    var currentValue = state.now

    val inputElement = input {
        addModifiers(mod)
        config()

        type = InputType.range
        min = range.first.toString()
        max = range.last.toString()
        this.step = step.toString()

        onInputFunction = {
            (it.target as HTMLInputElement).value.toIntOrNull()?.let { newValue ->
                if (newValue != currentValue) {
                    currentValue = newValue
                    state.set(newValue)
                }
            }
        }
    }

    fun display(value: Int) {
        if (value == currentValue) return
        currentValue = value
        inputElement.value = value.toString()
    }

    inputElement.value = currentValue.toString()

    launchEffect("slider") {
        state.flow.collect { display(it) }
    }
    return inputElement
}

fun ViewScope.slider(
    icon: Svg,
    state: MutableTap<Int>,
    range: IntRange = (0..100),
    step: Int = range.step,
    mod: ModifierSet? = null,
    config: INPUT.() -> Unit = { }
) = row(modify(mod, AlignItemsCenter)) {
    icon(icon, modify(Height3))
    slider(state, range, step, modify(Flex1), config)
}