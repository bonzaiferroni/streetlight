package koala.html

import koala.css.BackgroundImage
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.StyleProperty
import koala.css.StyleSet
import koala.css.UrlValue
import koala.css.applyModifiers
import koala.css.applyStyles
import koala.css.modify
import koala.css.styleOf
import kotlinx.html.*
import kotlinx.html.button as buttonTag

fun FlowContent.button(
    text: String,
    onClick: String? = null,
    id: Id? = null,
    modifiers: ModifierSet? = null,
    styles: StyleSet? = null,
    block: BUTTON.() -> Unit = {},
) {
    buttonTag {
        applyId(id)
        applyModifiers(modify(ElementClass.button, modifiers))
        applyStyles(styles)
        onClick?.let { this.onClick = it }
        +text
        block()
    }
}

fun FlowContent.button(
    text: String,
    route: AppRoute,
    modifiers: ModifierSet? = null,
    styles: StyleSet? = null,
    block: BUTTON.() -> Unit = {},
) {
    action(route) {
        button(text, modifiers = modifiers, block = block, styles = styles)
    }
}

fun FlowContent.button(
    text: String,
    route: AppRoute,
    background: String?,
    modifiers: ModifierSet? = null
) {
    val styles = background?.let {
        styleOf(StyleProperty.backgroundUrl to UrlValue(it))
    }
    button(text, route, modify(modifiers, BackgroundImage), styles)
}

fun FlowContent.button(
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

