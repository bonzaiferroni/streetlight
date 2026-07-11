package koala.dom

import koala.css.FadeLoop
import koala.css.ModifierSet
import koala.css.PointerEventsAuto
import koala.css.PointerEventsNone
import koala.css.Reveal
import koala.css.TextAlignCenter
import koala.css.Width100P
import koala.css.addModifiers
import koala.css.modify
import koala.html.DialogStyle
import koala.html.filigree
import koala.html.heading2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.js.dialog
import org.w3c.dom.HTMLDialogElement

fun AppScope.dialog(
    title: String? = null,
    stateFlow: Flow<Boolean>? = null,
    mod: ModifierSet? = null,
    onClose: (() -> Unit)? = null,
    content: (AppScope.(DialogElement) -> Unit)? = null
): DialogElement {
//    fun closeDialog() {
//        val dialog = dialog ?: return
//        close(dialog)
//    }

    val element = dialog {
        addModifiers(DialogStyle.Class, mod)
        // dialogContent(title, ::closeDialog, content)
    }

    return DialogElement(element, onClose, app, parentScope).also { dialog ->
        content?.let {
            dialog.updateContent(title, false, content)
        }

        element.addEventListener("click", { event ->
            if (event.target == element) {
                dialog.close()
            }
        })

        parentScope.launch {
            stateFlow?.collect {
                if (it) dialog.open() else dialog.close()
            }
        }
    }
}

private fun TagScope.dialogContent(
    title: String?,
    content: DIV.() -> Unit
) {
    column(modify(Width100P, PointerEventsNone)) {
        title?.let {
            filigree {
                heading2(title, modify(TextAlignCenter, PointerEventsAuto, FadeLoop))
            }
        }

        div(modify(DialogStyle.Content, PointerEventsAuto)) {
            content()
        }
    }
}

class DialogElement(
    val element: HTMLDialogElement,
    private val onClose: (() -> Unit)? = null,
    private val app: AppContainer,
    private val parentScope: CoroutineScope,
) {
    fun open() = this.also {
        element.open()
    }

    fun close() {
        parentScope.launch {
            element.unmodify(Reveal)
            delay(200)
            element.close()
            onClose?.invoke()
        }
    }

    fun updateContent(
        title: String?,
        open: Boolean,
        content: AppScope.(DialogElement) -> Unit,
    ) {
        element.replaceDynamicRender(app, parentScope) {
            dialogContent(title) {
                content(this@DialogElement)
            }
        }
        if (open) element.open()
    }
}

fun HTMLDialogElement.open() {
    showModal()
    modify(Reveal)
}

fun AppScope.dialogWithCard(
    title: String?,
    stateFlow: Flow<Boolean>? = null,
    mod: ModifierSet? = null,
    content: AppScope.(DialogElement) -> Unit
) = dialog(title, stateFlow, mod) {
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