package koala.component

import kampfire.model.Url
import koala.SvgFile
import koala.SvgPack
import koala.modifier.*
import kotlinx.html.*
import koala.model.AppRoute
import koala.page.Id

/**
 * A link styled as a button, to [route].
 *
 * [flair] is shown before the text, by default one chosen for common labels such as "tickets".
 */
fun FlowContent.btn(
    text: String,
    route: AppRoute,
    mod: Modifier? = null,
    flair: SvgPack? = labelFlairs[text.lowercase()],
    block: A.() -> Unit = {}
) {
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
    flair: SvgPack? = null,
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
 * [flair] is shown before the text, by default one chosen for the label or the link's domain.
 */
@Deprecated("use overload that takes url")
fun FlowContent.btn(
    text: String,
    href: String,
    mod: Modifier? = null,
    id: Id? = null,
    flair: SvgPack? = flairOf(text, href),
    block: A.() -> Unit = {},
) {
    navigation(
        href = href,
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
    flair: SvgPack? = flairOf(text, href.value),
    block: A.() -> Unit = {},
) = btn(text, href.value, mod, id, flair, block)

/** The host of [url] without a leading `www.`, or `null`. */
fun domainOf(url: String): String? =
    Regex("""^(?:[a-zA-Z][a-zA-Z\d+\-.]*://)?(?:[^@/\n]+@)?([^:/\n?#]+)""")
        .find(url.trim())
        ?.groupValues?.get(1)
        ?.removePrefix("www.")
        ?.takeIf { it.isNotBlank() }

/** The flair of the label [text], or of the domain of [href], or a link icon. */
private fun flairOf(text: String, href: String): SvgPack =
    labelFlairs[text.lowercase()] ?: domainOf(href)?.let { domainFlairs[it] } ?: SvgFile.Link

private val domainFlairs = mapOf(
    "youtube.com" to SvgFile.BrandYoutube,
    "en.wikipedia.org" to SvgFile.Book,
)

private val labelFlairs = mapOf(
    "tickets" to SvgFile.Ticket,
    "edit" to SvgFile.Edit,
    "website" to SvgFile.World,
    "calendar" to SvgFile.Calendar,
)
