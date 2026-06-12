package koala.dom

import kampfire.model.Url
import koala.SiteImage
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.IMG
import kotlinx.html.js.img
import kotlinx.html.style
import org.w3c.dom.HTMLImageElement

fun AppendScope.image(
    url: Url? = SiteImage.placeholderLg.url,
    modifiers: ModifierSet? = null,
    block: (IMG.() -> Unit)? = null
): HTMLImageElement {
    val initialSrc = url?.value ?: ""
    val element = img {
        this.src = initialSrc
        addModifiers(modifiers)
        if (initialSrc.isEmpty()) {
            style = "display: none;"
        }
        block?.invoke(this)
    }

    return element
}

fun RenderScope.image(
    binding: Flow<Url?>,
    initial: Url? = SiteImage.placeholderLg.url,
    modifiers: ModifierSet? = null,
    hideOnError: Boolean = true,
    block: (IMG.() -> Unit)? = null
): HTMLImageElement {
    val element = image(initial, modifiers, block)

    fun hideImage() {
        element.style.display = "none"
    }

    fun showImage() {
        element.style.removeProperty("display")
    }

    parentScope.launch {
        binding.collect { url ->
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