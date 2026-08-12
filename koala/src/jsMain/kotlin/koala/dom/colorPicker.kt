package koala.dom

import koala.css.*
import koala.model.MutableField
import kotlinx.html.InputType
import kotlinx.html.js.input
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement

fun ViewScope.colorPicker(
    text: String,
    colorState: MutableField<Rgb>,
    mod: ModifierSet? = null,
) {
    var currentColor: Rgb? = null
    lateinit var inputElement: HTMLInputElement
    lateinit var buttonElement: HTMLButtonElement

    fun display(color: Rgb) {
        if (currentColor == color) return
        currentColor = color
        val hex = color.toHex()
        inputElement.value = hex
        buttonElement.setStyle(Property.BackgroundColor.to(hex))
    }

    box(mod) {
        inputElement = input {
            addModifiers(Opacity0)

            type = InputType.color

            onInputFunction = {
                val newColor = rgbOf((it.target as HTMLInputElement).value)
                if (newColor != null && newColor != currentColor) {
                    colorState.set(newColor)
                    display(newColor)
                }
            }
        }
        buttonElement = button(text, { inputElement.click() })
    }

    display(colorState.now)

    launchEffect(::colorPicker) {
        colorState.flow.collect { value ->
            display(value)
        }
    }
}