package koala.dom

import koala.css.Css
import koala.css.applyModifiers
import kotlinx.html.DIALOG
import kotlinx.html.js.dialog

fun RenderContext.fullscreenBox(
    block: DIALOG.() -> Unit
) = dialog {
    applyModifiers(Css("fullscreen-box"))
    block()
}