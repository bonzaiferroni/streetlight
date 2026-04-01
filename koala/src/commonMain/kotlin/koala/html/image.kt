package koala.html

import koala.css.*
import kotlinx.html.*

fun FlowContent.image(
    id: Id,
    src: String? = null,
    modifiers: ModifierSet? = null,
    placeholder: String = SiteImage.placeholder,
    block: (IMG.() -> Unit)? = null
) {
    image(src, modifiers, placeholder) {
        this.id = id.identifier
        block?.invoke(this)
    }
}

fun FlowContent.image(
    src: String? = null,
    modifiers: ModifierSet? = null,
    placeholder: String = SiteImage.placeholder,
    block: (IMG.() -> Unit)? = null
) {
    img {
        this.src = src ?: placeholder
        addModifiers(modifiers)
        block?.invoke(this)
    }
}

object SiteImage {
    val placeholder = imagePathOf("placeholder.jpg")
    val placeholderThumb = imagePathOf("placeholder_thumb.jpg")
}

private fun imagePathOf(filename: String) = "/www/img/$filename"
