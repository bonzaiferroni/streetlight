package koala.html

import koala.interop.KoalaFun
import koala.modifier.*
import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.onClick

// a button per value of a root setting; each applies its value to the root element and localStorage
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

// the button of a switch carries its value under this attribute
val <T> Attribute<T>.optionAttribute
    get() = Attribute("$name-option", isCustom, toStringValue, toValue)

// restores the stored value of a root setting in the head, or applies the fallback
fun <T> rootSwitchScript(attribute: Attribute<T>, fallback: T) = jsScriptOf {
    invoke(KoalaFun.initRootSwitch, attribute.identifier, attribute.toStringValue(fallback))
}

// marks the switch button whose value matches the root
fun <T> rootSwitchCss(
    attribute: Attribute<T>,
    values: Iterable<T>,
    style: String = "color: var(--primary-fg);",
) = values.joinToString("\n") { value ->
    "${attribute.selector(value)} ${attribute.optionAttribute.selector(value)} { $style }"
}
