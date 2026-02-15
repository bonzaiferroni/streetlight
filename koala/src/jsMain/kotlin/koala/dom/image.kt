package koala.dom

import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.html.SiteImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.IMG
import kotlinx.html.js.img
import kotlinx.html.style
import org.w3c.dom.HTMLImageElement

fun RenderContext.image(
    src: String? = SiteImage.placeholderImage,
    modifiers: ModifierSet? = null,
    binding: Flow<String?>? = null,
    block: (IMG.() -> Unit)? = null
): HTMLImageElement {
    val element = img {
        this.src = src ?: ""
        applyModifiers(modifiers)
        if (src == null) {
            style = "display: none;"
        }
        block?.invoke(this)
    }

    renderScope.launch {
        binding?.collect { url ->
            if (url == null) {
                element.style.display = "none"
            } else {
                element.style.removeProperty("display")
                element.src = url
            }
        }
    }
    return element
}