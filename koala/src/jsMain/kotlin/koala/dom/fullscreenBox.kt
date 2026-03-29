package koala.dom

import koala.css.Class
import koala.css.addModifiers
import kotlinx.html.DIALOG
import kotlinx.html.js.dialog

fun RenderContext.fullscreenBox(
    block: DIALOG.() -> Unit
) = dialog {
    addModifiers(Class("fullscreen-box"))
    block()
}