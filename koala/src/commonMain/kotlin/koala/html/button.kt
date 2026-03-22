package koala.html

import koala.css.BackgroundImage
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.StyleProperty
import koala.css.StyleSet
import koala.css.UrlValue
import koala.css.modify
import koala.css.styleOf
import kotlinx.html.*

//fun FlowContent.button(
//    text: String,
//    onClick: String? = null,
//    id: Id? = null,
//    modifiers: ModifierSet? = null,
//    styles: StyleSet? = null,
//    block: BUTTON.() -> Unit = {},
//) {
//    buttonTag {
//        applyId(id)
//        applyModifiers(modify(ElementClass.button, modifiers))
//        applyStyles(styles)
//        onClick?.let { this.onClick = it }
//        +text
//        block()
//    }
//}

fun FlowContent.button(
    text: String,
    route: AppRoute,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    styles: StyleSet? = null,
    block: A.() -> Unit = {},
) {
    action(
        text = text,
        route = route,
        modifiers = modify(ElementClass.button, modifiers),
        id = id,
        block = block,
        styles = styles
    )
}

fun FlowContent.button(
    text: String,
    route: AppRoute,
    background: String?,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    styles: StyleSet? = null,
    block: A.() -> Unit = {},
) {
    val styles = background?.let {
        styleOf(styles, StyleProperty.backgroundUrl to UrlValue(it))
    } ?: styles
    button(
        text = text,
        route = route,
        modifiers = modify(modifiers, BackgroundImage),
        id = id,
        styles = styles,
        block = block
    )
}

fun FlowContent.button(
    text: String,
    href: String,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    styles: StyleSet? = null,
    addExternalIndicator: Boolean = true,
    block: A.() -> Unit = {},
) {
    val domain = href.takeIf { addExternalIndicator }?.let { domainOf(it) }
    val text = domain?.let { domain ->
        domainMap[domain].let { symbol ->
            "${symbol ?: "🔗"} $text"
        }
    } ?: text
    action(
        href = href,
        text = text,
        modifiers = modify(ElementClass.button, modifiers),
        block = block,
        id = id,
        styles = styles
    )
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

