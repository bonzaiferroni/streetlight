package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.html.*
import kotlinx.html.button as buttonCore

fun FlowOrInteractiveOrPhrasingContent.button(
    text: String,
    onClick: String? = null,
    id: Id? = null,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {},
) {
    buttonCore {
        applyModifiers(modify(ElementClass.button, modifiers))
        applyId(id)
        onClick?.let { this.onClick = it }
        +text
        block()
    }
}

fun FlowOrInteractiveOrPhrasingContent.button(
    text: String,
    route: AppRoute,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {},
) {
    action(route) {
        button(text, modifiers = modifiers, block = block)
    }
}

fun FlowOrInteractiveOrPhrasingContent.button(
    text: String,
    src: String,
    modifiers: ModifierSet? = null,
    block: BUTTON.() -> Unit = {},
) {
    val domain = domainOf(src)
    val text = domain?.let { domain ->
        domainMap[domain].let { symbol ->
            "${symbol ?: "🔗"} $text"
        }
    } ?: text
    action(src) {
        button(text, modifiers = modifiers, block = block)
    }
}

fun domainOf(url: String): String? =
    Regex("""^(?:[a-zA-Z][a-zA-Z\d+\-.]*://)?(?:[^@/\n]+@)?([^:/\n?#]+)""")
        .find(url.trim())
        ?.groupValues?.get(1)
        ?.removePrefix("www.")
        ?.takeIf { it.isNotBlank() }

private const val video = "📼"

private val domainMap = mapOf(
    "youtube.com" to video
)

