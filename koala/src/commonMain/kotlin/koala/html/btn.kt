package koala.html

import koala.css.BackgroundImage
import koala.css.Class
import koala.css.ModifierSet
import koala.css.StyleProperty
import koala.css.StyleSet
import koala.css.UrlValue
import koala.css.setStyle
import koala.css.modify
import kotlinx.html.*

fun FlowContent.btn(
    text: String,
    route: AppRoute,
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null,
) {
    action(
        text = text,
        route = route,
        modifiers = modify(BtnKey.Class, modifiers),
        block = block,
    )
}

fun FlowContent.btn(
    text: String,
    route: AppRoute,
    background: String?,
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null,
) {
    action(
        text = text,
        route = route,
        modifiers = modify(modifiers, BtnKey.Class, BackgroundImage),
    ) {
        background?.let {
            setStyle(StyleProperty.backgroundUrl.to(UrlValue(it)))
        }
        block?.invoke(this)
    }
}

fun FlowContent.btn(
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
        modifiers = modify(BtnKey.Class, modifiers),
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

object BtnKey {
    val Class = Class("btn")
}
