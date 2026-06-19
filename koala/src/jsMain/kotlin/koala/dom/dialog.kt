package koala.dom

import koala.css.AlignItemsStretch
import koala.css.FadeLoop
import koala.css.ModifierSet
import koala.css.PointerEventsAuto
import koala.css.PointerEventsNone
import koala.css.Reveal
import koala.css.TextAlignCenter
import koala.css.Width100P
import koala.css.addModifiers
import koala.css.modify
import koala.dom.column
import koala.dom.div
import koala.html.DialogStyle
import koala.html.filigree
import koala.html.heading2
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.DIALOG
import kotlinx.html.dom.append
import kotlinx.html.js.dialog
import org.w3c.dom.HTMLDialogElement

fun AppScope.dialog(
    title: String? = null,
    stateFlow: Flow<Boolean>? = null,
    modifiers: ModifierSet? = null,
    onClose: (() -> Unit)? = null,
    content: TagScope.(() -> Unit) -> Unit = { }
): DialogElement {

    fun close(dialog: HTMLDialogElement) {
        parentScope.launch {
            dialog.unmodify(Reveal)
            delay(200)
            dialog.close()
            onClose?.invoke()
        }
    }

    var dialog: HTMLDialogElement? = null
    fun closeDialog() {
        val dialog = dialog ?: return
        close(dialog)
    }

    dialog = dialog {
        addModifiers(DialogStyle.Class, modifiers)
        dialogContent(title, ::closeDialog, content)
    }

    dialog.addEventListener("click", { event ->
        if (event.target == dialog) {
            close(dialog)
        }
    })

    parentScope.launch {
        stateFlow?.collect {
            if (it) dialog.open() else close(dialog)
        }
    }

    return DialogElement(dialog) { close(dialog) }
}

internal fun TagScope.dialogContent(
    title: String?,
    closeDialog: () -> Unit,
    content: TagScope.(() -> Unit) -> Unit
) {
    column(modify(Width100P, PointerEventsNone)) {
        title?.let {
            filigree {
                heading2(title, modify(TextAlignCenter, PointerEventsAuto, FadeLoop))
            }
        }

        div(modify(DialogStyle.Content, PointerEventsAuto)) {
            content(closeDialog)
        }
    }
}

data class DialogElement(val element: HTMLDialogElement, val close: () -> Unit) {
    fun open() = this.also {
        element.open()
    }

    fun updateContent(
        title: String?,
        content: TagScope.(() -> Unit) -> Unit,
    ) {
        element.clear()
        element.append {
            dialogContent(title, close, content)
        }
        element.open()
    }
}

fun HTMLDialogElement.open() {
    showModal()
    modify(Reveal)
}

fun AppScope.dialogWithCard(
    title: String?,
    stateFlow: Flow<Boolean>? = null,
    modifiers: ModifierSet? = null,
    onClose: (() -> Unit)? = null,
    content: AppScope.(() -> Unit) -> Unit
) = dialog(title, stateFlow, modifiers, onClose) {
    dialogCard {
        content(it)
    }
}

fun TagScope.dialogCard(
    modifiers: ModifierSet? = null,
    content: TagScope.() -> Unit
) = card(modify(DialogStyle.Card, modifiers)) {
    content()
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