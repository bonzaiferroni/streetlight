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
    initial: String? = SiteImage.placeholderImage,
    modifiers: ModifierSet? = null,
    binding: Flow<String?>? = null,
    block: (IMG.() -> Unit)? = null
): HTMLImageElement {
    val element = img {
        this.src = initial ?: ""
        applyModifiers(modifiers)
        if (initial.isNullOrBlank()) {
            style = "display: none;"
        }
        block?.invoke(this)
    }

    renderScope.launch {
        binding?.collect { url ->
            val url = url?.takeIf { it.isNotBlank() } ?: initial
            if (url.isNullOrBlank()) {
                element.style.display = "none"
            } else {
                element.style.removeProperty("display")
                element.src = url
            }
        }
    }
    return element
}