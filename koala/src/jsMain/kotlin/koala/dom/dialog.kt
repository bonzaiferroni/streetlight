package koala.dom

import koala.css.AlignItemsStretch
import koala.css.ModifierSet
import koala.css.Reveal
import koala.css.TextAlignCenter
import koala.css.addModifiers
import koala.css.modify
import koala.html.DialogKey
import koala.html.filigree
import koala.html.heading3
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.js.dialog
import org.w3c.dom.HTMLDialogElement

fun DOMRender.dialogBox(
    title: String?,
    stateFlow: Flow<Boolean>? = null,
    modifiers: ModifierSet? = null,
    onClose: (() -> Unit)? = null,
    block: DOMRender.(() -> Unit) -> Unit
): HTMLDialogElement {

    fun close(dialog: HTMLDialogElement) {
        renderScope.launch {
            dialog.unmodify(Reveal)
            delay(200)
            dialog.close()
            onClose?.invoke()
        }
    }

    var dialog: HTMLDialogElement? = null
    dialog = dialog {
        addModifiers(DialogKey.Class, modifiers)
        column(modify(AlignItemsStretch)) {
            title?.let {
                filigree {
                    heading3(title, modify(TextAlignCenter))
                }
            }
            fun closeImage() {
                val dialog = dialog ?: return
                close(dialog)
            }
            block(::closeImage)
        }
    }

//    dialog.addEventListener("click", { event ->
//        val mouse = event as MouseEvent
//        val rect = dialog.getBoundingClientRect()
//
//        val inside =
//            mouse.clientY >= rect.top &&
//                    mouse.clientY <= rect.top + rect.height &&
//                    mouse.clientX >= rect.left &&
//                    mouse.clientX <= rect.left + rect.width
//
//        if (!inside) {
//            close(dialog)
//        }
//    })

    dialog.addEventListener("click", { event ->
        if (event.target == dialog) {
            close(dialog)
        }
    })

    renderScope.launch {
        stateFlow?.collect {
            if (it) dialog.open() else close(dialog)
        }
    }

    return dialog
}

fun HTMLDialogElement.open() {
    showModal()
    modify(Reveal)
}