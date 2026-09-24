package koala.dom

import kampfire.model.Url
import koala.Image
import koala.SiteImage
import koala.modifier.*
import kampfire.model.Tap
import kotlinx.html.IMG
import kotlinx.html.js.img
import kotlinx.html.style
import web.events.ERROR
import web.events.Event
import web.events.LOAD
import web.events.addEventListener
import web.html.HTMLImageElement

/** The image at [url], hidden while there is none. */
fun AppendScope.image(
    url: Url? = SiteImage.placeholderLg,
    mod: Modifier? = null,
    block: (IMG.() -> Unit)? = null
): HTMLImageElement {
    val initialSrc = url?.value ?: ""
    val element = img {
        this.src = initialSrc
        addModifiers(mod)
        if (initialSrc.isEmpty()) {
            style = "display: none;"
        }
        block?.invoke(this)
    }.asWeb()

    return element
}

/** The image of [state], hidden while it is `null` and, with [hideOnError], while it fails to load. */
fun ViewScope.image(
    state: Tap<Image?>,
    mod: Modifier? = null,
    hideOnError: Boolean = true,
    block: (IMG.() -> Unit)? = null
): HTMLImageElement {
    val element = image(state.now?.url ?: SiteImage.placeholderLg, mod, block)

    fun hideImage() {
        element.style.display = "none"
    }

    fun showImage() {
        element.style.removeProperty("display")
    }

    launchEffect("image") {
        state.flow.collect { url ->
            val url = url?.value ?: ""
            if (url.isEmpty()) {
                hideImage()
            } else {
                showImage()
                element.src = url
            }
        }
    }

    if (hideOnError) {
        element.addEventListener(Event.ERROR, {
            hideImage()
        })
        element.addEventListener(Event.LOAD, {
            showImage()
        })
    }

    return element
}