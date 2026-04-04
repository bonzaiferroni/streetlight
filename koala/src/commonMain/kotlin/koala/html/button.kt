package koala.html

import koala.Svg
import koala.css.*
import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.button as buttonTag

fun FlowContent.button(
    text: String,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    buttonTag {
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
    svg: Svg,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    buttonTag {
        configureSvgButton(svg, modifiers, block)
    }
}

fun BUTTON.configureSvgButton(
    svg: Svg,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    addModifiers(ButtonKey.IconClass, modify(IconKey.Class, modifiers))
    setStyle(Property.ImageUrl.to(UrlValue(svg)))
    block()
}

fun FlowContent.button(
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    buttonTag {
        addModifiers(ButtonKey.ElementClass, modifiers)
        block()
    }
}

object ButtonKey {
    val IconClass = Class("icon-button")
    val ElementClass = Class("image-button")
}

// language="CSS"
val IconButtonCss get() = """
${ButtonKey.IconClass} {
    color: inherit;
    transition: background-color var(--magic-interval) var(--magic-easing);
}

${ButtonKey.IconClass}:hover {
    background-color: rgb(var(--accent));
    cursor: pointer;
}

${ButtonKey.ElementClass} {
    appearance: none;
    -webkit-appearance: none;

    background: none;
    border: none;
    padding: 0;
    margin: 0;

    font: inherit;
    color: inherit;
    text-align: inherit;

    outline: none;

    cursor: pointer;
}

${ButtonKey.ElementClass}:focus-visible {
    outline: 2px solid currentColor;
}
"""