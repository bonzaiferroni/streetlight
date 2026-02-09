package koala.dom

import koala.css.AlignItemsStretch
import koala.css.Css
import koala.css.Reveal
import koala.css.TextAlignCenter
import koala.css.applyModifiers
import koala.css.modify
import koala.html.heading3
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.DIALOG
import kotlinx.html.js.dialog
import org.w3c.dom.HTMLDialogElement

fun RenderContext.dialogBox(
    title: String?,
    block: RenderContext.() -> Unit
) {
    var dialog: HTMLDialogElement? = null
    dialog = dialog {
        applyModifiers(Css("dialog-box"))
        column(modify(AlignItemsStretch)) {
            title?.let {
                heading3(title, modify(TextAlignCenter))
            }
            block()
            button("hide") {
                val dialog = dialog ?: error("no dialog")
                renderScope.launch {
                    dialog.unmodify(Reveal)
                    delay(200)
                    dialog.close()
                }
            }
        }
    }

    button("show") {
        dialog.showModal()
        dialog.modify(Reveal)
    }
}