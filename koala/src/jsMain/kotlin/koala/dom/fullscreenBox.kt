package koala.dom

import koala.css.Css
import koala.css.setModifiers
import kotlinx.html.DIALOG
import kotlinx.html.js.dialog

fun RenderContext.fullscreenBox(
    block: DIALOG.() -> Unit
) = dialog {
    setModifiers(Css("fullscreen-box"))
    block()
}