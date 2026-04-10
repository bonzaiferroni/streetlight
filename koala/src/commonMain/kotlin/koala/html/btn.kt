package koala.html

import kampfire.model.Url
import koala.css.BackgroundImage
import koala.css.Class
import koala.css.ModifierSet
import koala.css.Property
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
    val prefix = labelPrefixMap[text.lowercase()]
    val text = prefix?.let {
        "$it $text"
    } ?: text

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
    background: Url?,
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null,
) {
    action(
        text = text,
        route = route,
        modifiers = modify(modifiers, BtnKey.Class, BackgroundImage),
    ) {
        background?.let {
            setStyle(Property.BackgroundUrl.to(UrlValue(it)))
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
    val prefix = labelPrefixMap[text.lowercase()] ?: domain?.let { domain ->
        domainPrefixMap[domain] ?: "🔗"
    }
    val text = prefix?.let {
        "$it $text"
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

private val domainPrefixMap = mapOf(
    "youtube.com" to video
)

private val labelPrefixMap = mapOf(
    "tickets" to "🎟",
    "edit" to "✍",
)

object BtnKey {
    val Class = Class("btn")
}
