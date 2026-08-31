package koala.dom

import koala.css.*
import kampfire.model.MutableTap
import kotlinx.html.InputType
import kotlinx.html.js.input
import kotlinx.html.js.onInputFunction
import web.html.HTMLButtonElement
import web.html.HTMLInputElement

fun ViewScope.colorPicker(
    text: String,
    colorState: MutableTap<Rgb>,
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
                val newColor = rgbOfOrNull((it.target as HTMLInputElement).value)
                if (newColor != null && newColor != currentColor) {
                    colorState.set(newColor)
                    display(newColor)
                }
            }
        }.asWeb()
        buttonElement = button(text, { inputElement.click() }, modify(Secondary))
    }

    display(colorState.now)

    launchEffect(::colorPicker) {
        colorState.flow.collect { value ->
            display(value)
        }
    }
}