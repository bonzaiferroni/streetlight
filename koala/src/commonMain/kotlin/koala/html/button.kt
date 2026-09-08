package koala.html

import koala.Svg
import koala.css.*
import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.button as buttonTag

fun FlowContent.button(
    text: String,
    modifiers: ModifierSet? = null,
    flair: String? = null,
    block: BUTTON.() -> Unit = {}
) {
    buttonTag {
        configureButton(text, modifiers, flair, block)
    }
}

fun BUTTON.configureButton(
    text: String,
    modifiers: ModifierSet? = null,
    flair: String? = null,
    block: BUTTON.() -> Unit = {}
) {
    addModifiers(BtnKey.Class, modifiers)
    block()
    flair?.let {
        span {
            +flair
        }
    }
    span {
        +text
    }
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
    addModifiers(ButtonStyle.IconClass, modify(IconStyle.Icon, modifiers))
    setStyle(Property.MaskUrl.to(svg))
    block()
}

fun FlowContent.button(
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    buttonTag {
        configureElementButton(modifiers, block)
    }
}

fun BUTTON.configureElementButton(
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    addModifiers(ButtonStyle.ElementClass, modifiers)
    block()
}

object ButtonStyle {
    val IconClass = Class("icon-button")
    val ElementClass = Class("element-button")
}

// language="CSS"
val IconButtonCss get() = """
${ButtonStyle.IconClass} {
    color: inherit;
    transition: background-color var(--magic-interval) var(--magic-easing);
}

${ButtonStyle.IconClass}:hover {
    background-color: rgb(var(--accent));
    cursor: pointer;
}

${ButtonStyle.ElementClass} {
    display: inline-flex;
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

${ButtonStyle.ElementClass}:focus-visible {
    outline: 2px solid currentColor;
}
"""