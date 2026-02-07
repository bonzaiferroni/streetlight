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
    src: String = DefaultPath.placeholderImage,
    modifiers: ModifierSet? = null,
    block: (IMG.() -> Unit)? = null
) {
    img {
        this.src = "/www/img/${src}"
        applyModifiers(modifiers)
        block?.invoke(this)
    }
}

object DefaultPath {
    val placeholderImage = "placeholder.jpg"
}