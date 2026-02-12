package koala.html

import koala.css.*
import kotlinx.html.*

fun FlowContent.image(
    id: Id,
    src: String,
    modifiers: ModifierSet? = null,
    block: (IMG.() -> Unit)? = null
) {
    image(src, modifiers) {
        this.id = id.value
        block?.invoke(this)
    }
}

fun FlowContent.image(
    src: String = SiteImage.placeholderImage,
    modifiers: ModifierSet? = null,
    block: (IMG.() -> Unit)? = null
) {
    img {
        this.src = src
        applyModifiers(modifiers)
        block?.invoke(this)
    }
}

object SiteImage {
    val placeholderImage = imagePathOf("placeholder.jpg")
}

private fun imagePathOf(filename: String) = "/www/img/$filename"