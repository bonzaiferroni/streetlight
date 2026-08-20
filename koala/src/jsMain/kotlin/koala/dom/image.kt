package koala.dom

import kampfire.model.Url
import koala.Image
import koala.SiteImage
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.model.Tap
import kotlinx.html.IMG
import kotlinx.html.js.img
import kotlinx.html.style
import org.w3c.dom.HTMLImageElement

fun AppendScope.image(
    url: Url? = SiteImage.placeholderLg,
    mod: ModifierSet? = null,
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
    }

    return element
}

fun ViewScope.image(
    state: Tap<Image?>,
    mod: ModifierSet? = null,
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
        element.addEventListener("error", {
            hideImage()
        })
        element.addEventListener("load", {
            showImage()
        })
    }

    return element
}