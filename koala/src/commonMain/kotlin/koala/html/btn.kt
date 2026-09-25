package koala.html

import kampfire.model.Url
import koala.modifier.*
import kotlinx.html.*

/**
 * A link styled as a button, to [route].
 *
 * A flair is shown before the text: [flair] when given, or one chosen for common labels such as "tickets" unless
 * [addFlair] is off.
 */
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
        mod = modify(ButtonStyle.Class, mod),
        flair = flair,
    ) {
        block()
        span {
            +text
        }
    }
}

/** A link styled as a button, to [route], over a blurred [background] image. */
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
        mod = modify(mod, ButtonStyle.Class, BackgroundImage),
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

/**
 * A link styled as a button, to [href].
 *
 * A flair is shown before the text: [flair] when given, or one chosen for the label or the link's domain unless
 * [addFlair] is off.
 */
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
        mod = modify(ButtonStyle.Class, mod),
        flair = flair,
        id = id,
    ) {
        block()
        span {
            +text
        }
    }
}

/** A link styled as a button, to [href], with a flair chosen as the text overload does. */
fun FlowContent.btn(
    text: String,
    href: Url,
    mod: Modifier? = null,
    id: Id? = null,
    addFlair: Boolean = true,
    flair: String? = null,
    block: A.() -> Unit = {},
) = btn(text, href.value, mod, id, addFlair, flair, block)

/** The host of [url] without a leading `www.`, or `null`. */
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

