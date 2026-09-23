package koala.html

import kampfire.model.Url
import koala.modifier.*
import kotlinx.html.*

fun FlowContent.btn(
    text: String,
    route: AppRoute,
    mod: Modifier? = null,
    addFlair: Boolean = true,
    flair: String? = null,
    block: A.() -> Unit = {}
) {
    val flair = flair ?: if (addFlair) labelPrefixMap[text.lowercase()] else null
    navigation(
        route = route,
        mod = modify(BtnStyle.Class, mod),
        flair = flair,
    ) {
        block()
        span {
            +text
        }
    }
}

fun FlowContent.btn(
    text: String,
    route: AppRoute,
    background: Url?,
    mod: Modifier? = null,
    flair: String? = null,
    block: A.() -> Unit = {}
) {
    navigation(
        route = route,
        flair = flair,
        mod = modify(mod, BtnStyle.Class, BackgroundImage),
    ) {
        background?.let {
            setStyle(Css.BackgroundUrl.of(it))
        }
        block()
        span {
            +text
        }
    }
}

@Deprecated("use overload that takes url")
fun FlowContent.btn(
    text: String,
    href: String,
    mod: Modifier? = null,
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
        mod = modify(BtnStyle.Class, mod),
        flair = flair,
        id = id,
    ) {
        block()
        span {
            +text
        }
    }
}

fun FlowContent.btn(
    text: String,
    href: Url,
    mod: Modifier? = null,
    id: Id? = null,
    addFlair: Boolean = true,
    flair: String? = null,
    block: A.() -> Unit = {},
) = btn(text, href.value, mod, id, addFlair, flair, block)

fun domainOf(url: String): String? =
    Regex("""^(?:[a-zA-Z][a-zA-Z\d+\-.]*://)?(?:[^@/\n]+@)?([^:/\n?#]+)""")
        .find(url.trim())
        ?.groupValues?.get(1)
        ?.removePrefix("www.")
        ?.takeIf { it.isNotBlank() }

private const val video = "📼"

private val domainPrefixMap = mapOf(
    "youtube.com" to video,
    "en.wikipedia.org" to "📖",
)

private val labelPrefixMap = mapOf(
    "tickets" to "🎟",
    "edit" to "✍",
    "website" to "🌐",
    "calendar" to "📅",
)

object BtnStyle {
    val Class = Class("btn")
}
