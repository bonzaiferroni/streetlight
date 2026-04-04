package koala.dom

import koala.Image
import koala.SiteImage
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.IMG
import kotlinx.html.js.img
import kotlinx.html.style
import org.w3c.dom.HTMLImageElement

fun RenderContext.image(
    initial: String? = SiteImage.placeholder.path,
    modifiers: ModifierSet? = null,
    binding: Flow<String?>? = null,
    hideOnError: Boolean = true,
    block: (IMG.() -> Unit)? = null
): HTMLImageElement {
    val initial = initial ?: ""
    val element = img {
        this.src = initial
        addModifiers(modifiers)
        if (initial.isEmpty()) {
            style = "display: none;"
        }
        block?.invoke(this)
    }

    fun hideImage() {
        element.style.display = "none"
    }

    fun showImage() {
        element.style.removeProperty("display")
    }

    renderScope.launch {
        binding?.collect { url ->
            val url = url?.takeIf { it.isNotBlank() } ?: initial
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