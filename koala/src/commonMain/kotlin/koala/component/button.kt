package koala.component

import koala.Svg
import koala.SvgPack
import koala.modifier.*
import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.button as buttonTag

/** A button styled as a [btn], showing [text] after an optional [flair]. */
fun FlowContent.button(
    text: String,
    mod: Modifier? = null,
    flair: SvgPack? = null,
    block: BUTTON.() -> Unit = {}
) {
    buttonTag {
        configureButton(text, mod, flair, block)
    }
}

/** Configures this element as a button styled as a [btn], showing [text] after an optional [flair]. */
fun BUTTON.configureButton(
    text: String,
    mod: Modifier? = null,
    flair: SvgPack? = null,
    block: BUTTON.() -> Unit = {}
) {
    addModifiers(ButtonStyle.Class, mod)
    block()
    flair?.let { flairIcon(it) }
    span {
        +text
    }
}

/** A button showing only the icon [svg]. */
fun FlowContent.button(
    svg: Svg,
    mod: Modifier? = null,
    block: BUTTON.() -> Unit = {}
) {
    buttonTag {
        configureSvgButton(svg, mod, block)
    }
}

/** Configures this element as a button showing only the icon [svg]. */
fun BUTTON.configureSvgButton(
    svg: Svg,
    mod: Modifier? = null,
    block: BUTTON.() -> Unit = {}
) {
    addModifiers(ButtonStyle.IconClass, modify(IconStyle.Icon, mod))
    setStyle(Css.MaskUrl.of(svg))
    block()
}

/** A button with no appearance of its own, holding whatever [block] builds. */
fun FlowContent.button(
    mod: Modifier? = null,
    block: BUTTON.() -> Unit = {}
) {
    buttonTag {
        configureElementButton(mod, block)
    }
}

/** Configures this element as a button with no appearance of its own. */
fun BUTTON.configureElementButton(
    mod: Modifier? = null,
    block: BUTTON.() -> Unit = {}
) {
    addModifiers(ButtonStyle.ElementClass, mod)
    block()
}

/** The [flair] of a [btn], as an icon in a span. */
internal fun FlowContent.flairIcon(flair: SvgPack) {
    span(modify(IconStyle.Icon, ButtonStyle.Flair, SmallIconHeight)) {
        setStyle(Css.MaskUrl.of(flair.small))
    }
}

object ButtonStyle {
    val Class = Class("btn")
    val Flair = Class("btn-flair")
    val IconClass = Class("icon-button")
    val ElementClass = Class("element-button")
}

// language="CSS"
val IconButtonCss get() = """
${ButtonStyle.IconClass} {
    color: inherit;
    transition: var(--transition-background-color);
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