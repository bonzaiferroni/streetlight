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
        addModifiers(BtnKey.Class, modifiers)
        block()
        +text
    }
}

fun FlowContent.button(
    svg: Svg,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {}
) {
    button {
        addModifiers(IconButtonKey.Class, modify(IconKey.Class, modifiers))
        setStyle(Property.MaskUrl.to(UrlValue(svg)))
        block()
    }
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