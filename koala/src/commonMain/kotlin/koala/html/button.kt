package koala.html

import koala.Svg
import koala.css.*
import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.button

fun FlowContent.button(
    text: String,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    button {
        configureButton(text, modifiers, block)
    }
}

fun BUTTON.configureButton(
    text: String,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    addModifiers(BtnKey.Class, modifiers)
    block()
    +text
}

fun FlowContent.button(
    svg: Svg? = null,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    button {
        configureButton(svg, modifiers, block)
    }
}

fun BUTTON.configureButton(
    svg: Svg? = null,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    addModifiers(IconButtonKey.Class, modify(IconKey.Class, modifiers))
    svg?.let {
        setStyle(Property.MaskUrl.to(UrlValue(svg)))
    }
    block()
}

object IconButtonKey {
    val Class = Class("icon-button")
}

// language="CSS"
val IconButtonCss get() = """
${IconButtonKey.Class} {
    color: inherit;
    transition: background-color var(--magic-interval) var(--magic-easing);
}

${IconButtonKey.Class}:hover {
    background-color: rgb(var(--accent));
    cursor: pointer;
}
"""