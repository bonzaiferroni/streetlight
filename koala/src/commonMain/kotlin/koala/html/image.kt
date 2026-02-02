package koala.html

import koala.css.*
import kotlinx.html.*

fun FlowContent.image(
    id: Id,
    src: String,
    vararg modifiers: CssClass,
    block: (IMG.() -> Unit)? = null
) {
    image(src, *modifiers) {
        this.id = id.value
        block?.invoke(this)
    }
}

fun FlowContent.image(
    src: String,
    vararg modifiers: CssClass,
    block: (IMG.() -> Unit)? = null
) {
    img {
        this.src = "/www/${src}"
        applyModifiers(*modifiers)
        block?.invoke(this)
    }
}