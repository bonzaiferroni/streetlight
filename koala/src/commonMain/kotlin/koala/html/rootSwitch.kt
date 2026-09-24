package koala.html

import koala.interop.KoalaFun
import koala.modifier.*
import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.onClick

/**
 * Renders a row of buttons, one per value of the root setting [attribute].
 *
 * Each button applies its value to the root element and to `localStorage`, and [content] builds what it shows.
 * [rootSwitchCss] marks the selected button.
 */
inline fun <reified T : Enum<T>> FlowContent.rootSwitch(
    attribute: Attribute<T>,
    mod: Modifier? = null,
    crossinline content: BUTTON.(T) -> Unit,
) {
    row(mod) {
        enumValues<T>().forEach { value ->
            button {
                setAttribute(attribute.optionAttribute.to(value))
                onClick = KoalaFun.applyRootSwitch.invokeJs(attribute.identifier, attribute.toStringValue(value))
                content(value)
            }
        }
    }
}

/** The attribute a [rootSwitch] button carries its value under, named for this root setting. */
val <T> Attribute<T>.optionAttribute
    get() = Attribute("$name-option", isCustom, toStringValue, toValue)

/**
 * CSS that applies [style] to the [rootSwitch] button whose value matches the root setting [attribute].
 *
 * The app places it in its own stylesheet.
 */
fun <T> rootSwitchCss(
    attribute: Attribute<T>,
    values: Iterable<T>,
    style: String = "color: var(--primary-fg);",
) = values.joinToString("\n") { value ->
    "${attribute.selector(value)} ${attribute.optionAttribute.selector(value)} { $style }"
}
