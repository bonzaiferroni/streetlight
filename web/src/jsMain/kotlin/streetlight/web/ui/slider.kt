package streetlight.web.ui

import koala.css.ModifierSet
import koala.css.addModifiers
import koala.dom.ViewScope
import koala.model.MutableTap
import kotlinx.html.InputType
import kotlinx.html.js.input
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement

fun ViewScope.slider(
    valueState: MutableTap<Int>,
    range: IntRange,
    step: Int = range.step,
    mod: ModifierSet? = null,
) {
    val inputElement = input {
        addModifiers(mod)

        type = InputType.range
        min = range.first.toString()
        max = range.last.toString()
        this.step = step.toString()

        onInputFunction = {
            (it.target as HTMLInputElement).value.toIntOrNull()?.let { newValue ->
                valueState.set(newValue)
            }
        }
    }

    inputElement.value = valueState.now.toString()

    launchEffect("slider") {
        valueState.flow.collect { value ->
            val text = value.toString()
            if (inputElement.value != text) inputElement.value = text
        }
    }
}