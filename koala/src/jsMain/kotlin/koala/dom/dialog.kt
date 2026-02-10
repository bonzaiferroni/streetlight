package koala.dom

import koala.css.AlignItemsStretch
import koala.css.Css
import koala.css.Reveal
import koala.css.TextAlignCenter
import koala.css.applyModifiers
import koala.css.modify
import koala.html.heading3
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIALOG
import kotlinx.html.js.dialog
import org.w3c.dom.HTMLDialogElement

fun RenderContext.dialogBox(
    title: String?,
    stateFlow: Flow<Boolean>? = null,
    block: RenderContext.(() -> Unit) -> Unit
): HTMLDialogElement {
    var dialog: HTMLDialogElement? = null
    dialog = dialog {
        applyModifiers(Css("dialog-box"))
        column(modify(AlignItemsStretch)) {
            title?.let {
                heading3(title, modify(TextAlignCenter))
            }
            fun closeImage() {
                val dialog = dialog ?: return
                renderScope.launch {
                    dialog.unmodify(Reveal)
                    delay(200)
                    dialog.close()
                }
            }
            block(::closeImage)
        }
    }

    renderScope.launch {
        stateFlow?.collect {
            if (it) dialog.showModal() else dialog.close()
        }
    }

    return dialog
}

fun HTMLDialogElement.open() {
    showModal()
    modify(Reveal)
}