package koala.dom

import koala.css.FadeLoop
import koala.css.MagicStyle
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
import koala.model.MutableTap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.js.dialog
import web.events.addEventListener
import web.html.HTMLDialogElement
import web.pointer.CLICK
import web.pointer.PointerEvent
import kotlin.time.Duration.Companion.milliseconds

fun ViewScope.dialog(
    state: MutableTap<Boolean>,
    mod: ModifierSet? = null,
    content: ViewScope.() -> Unit
): HTMLDialogElement {

    val element = dialog {
        addModifiers(DialogStyle.Class, mod)
    }.unsafeCast<HTMLDialogElement>()

    var closeJob: Job? = null
    fun closeDialog() {
        closeJob?.cancel()
        closeJob = launchEffect {
            element.unmodify(Reveal)
            delay(MagicStyle.Interval.milliseconds)
            element.close()
        }
        if (state.now)
            state.set(false)
    }

    fun openDialog() {
        closeJob?.cancel()
        mountChildView("dialog", element) {
            content()
        }
        element.open()
        if (!state.now)
            state.set(true)
    }

    element.addEventListener(PointerEvent.CLICK, { event ->
        if (event.target == element) {
            closeDialog()
        }
    })

    launchEffect(::dialog) {
        state.flow.collect { isOpen ->
            if (isOpen) {
                openDialog()
            } else {
                closeDialog()
            }
        }
    }

    return element
}

fun ViewScope.dialogContent(
    title: String?,
    mod: ModifierSet? = null,
    content: ViewScope.() -> Unit
) = rawDialogContent(title) {
    dialogCard(mod) {
        content()
    }
}

fun ViewScope.rawDialogContent(
    title: String?,
    mod: ModifierSet? = null,
    content: ViewScope.() -> Unit
) = column(modify(Width100P, PointerEventsNone)) {
    title?.let {
        filigree {
            heading2(title, modify(TextAlignCenter, PointerEventsAuto, FadeLoop))
        }
    }

    div(modify(mod, DialogStyle.Content, PointerEventsAuto)) {
        content()
    }
}

fun ViewScope.dialogCard(
    mod: ModifierSet? = null,
    content: ViewScope.() -> Unit
) = card(modify(DialogStyle.Card, mod)) {
    content()
}

fun HTMLDialogElement.open() {
    showModal()
    modify(Reveal)
}