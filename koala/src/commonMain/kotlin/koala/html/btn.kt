package koala.html

import kampfire.model.Url
import koala.css.BackgroundImage
import koala.css.Class
import koala.css.ModifierSet
import koala.css.Property
import koala.css.UrlValue
import koala.css.setStyle
import koala.css.modify
import kotlinx.html.*

fun FlowContent.btn(
    text: String,
    route: AppRoute,
    modifiers: ModifierSet? = null,
    addFlair: Boolean = true,
    flair: String? = null,
    block: A.() -> Unit = {}
) {
    val flair = flair ?: if (addFlair) labelPrefixMap[text.lowercase()] else null
    navigation(
        route = route,
        modifiers = modify(BtnKey.Class, modifiers),
        flair = flair,
        block = block,
    )
}

fun FlowContent.btn(
    text: String,
    route: AppRoute,
    background: Url?,
    modifiers: ModifierSet? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) {
    navigation(
        route = route,
        flair = flair,
        modifiers = modify(modifiers, BtnKey.Class, BackgroundImage),
    ) {
        background?.let {
            setStyle(Property.BackgroundUrl.to(it))
        }
        block()
    }
}

fun FlowContent.btn(
    text: String,
    href: String,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    addFlair: Boolean = true,
    flair: String? = null,
    block: A.() -> Unit = {},
) {
    val flair =
        flair ?: labelPrefixMap[text.lowercase()] ?: href.takeIf { addFlair }?.let { domainOf(it) }?.let { domain ->
            domainPrefixMap[domain] ?: "🔗"
        }

    navigation(
        href = href,
        modifiers = modify(BtnKey.Class, modifiers),
        block = block,
        flair = flair,
        id = id,
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
    "website" to "🌐",
    "calendar" to "📅",
)

object BtnKey {
    val Class = Class("btn")
}
