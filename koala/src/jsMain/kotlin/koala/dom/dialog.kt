package koala.dom

import koala.modifier.*
import koala.html.DialogStyle
import koala.html.filigree
import koala.html.heading2
import kampfire.model.MutableTap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import koala.model.Portal
import kotlinx.css.pct
import kotlinx.html.js.dialog
import web.events.addEventListener
import web.html.HTMLDialogElement
import web.events.CANCEL
import web.events.Event
import web.pointer.CLICK
import web.pointer.PointerEvent
import kotlin.time.Duration.Companion.milliseconds

/**
 * A modal dialog that is open while [state] is `true`, holding a child view built by [content] each time it
 * opens.
 *
 * A click on the backdrop or Escape closes it, and so does a new route, since the page behind it has changed.
 */
fun ViewScope.dialog(
    state: MutableTap<Boolean>,
    mod: Modifier? = null,
    content: ViewScope.() -> Unit
): HTMLDialogElement {
    val element = dialog {
        addModifiers(DialogStyle.Class, mod)
    }.unsafeCast<HTMLDialogElement>()

    var closeJob: Job? = null
    fun closeDialog() {
        closeJob?.cancel()
        closeJob = launchEffect {
            try {
                element.unmodify(Reveal)
                delay(MagicStyle.Interval.milliseconds)
            } finally {
                element.close()
            }
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
        // through the state alone, so the close runs once and is not cut short
        if (event.target == element) {
            state.set(false)
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

    // Escape closes the dialog through its state too, rather than natively, at once and out of step with it
    element.addEventListener(Event.CANCEL, { event ->
        event.preventDefault()
        state.set(false)
    })

    app.getOrNull<Portal>()?.let { portal ->
        launchEffect {
            portal.routeState.flow.drop(1).collect {
                if (state.now) state.set(false)
            }
        }
    }

    onDispose {
        element.close()
    }

    return element
}

/** The content of a [dialog]: [title] in a filigree over a card holding what [content] builds. */
fun ViewScope.dialogContent(
    title: String?,
    mod: Modifier? = null,
    content: ViewScope.() -> Unit
) = rawDialogContent(title) {
    dialogCard(mod) {
        content()
    }
}

/** The content of a [dialog]: [title] in a filigree over what [content] builds, with no card. */
fun ViewScope.rawDialogContent(
    title: String?,
    mod: Modifier? = null,
    content: ViewScope.() -> Unit
) = column(modify(Width(100.pct), PointerEventsNone)) {
    title?.let {
        filigree {
            heading2(title, modify(TextAlignCenter, PointerEventsAuto, FadeLoop))
        }
    }

    div(modify(mod, DialogStyle.Content, PointerEventsAuto)) {
        content()
    }
}

/** A card styled for a [dialog]. */
fun ViewScope.dialogCard(
    mod: Modifier? = null,
    content: ViewScope.() -> Unit
) = card(modify(DialogStyle.Card, mod)) {
    content()
}

/** Shows the dialog as modal and reveals it. */
fun HTMLDialogElement.open() {
    showModal()
    modify(Reveal)
}