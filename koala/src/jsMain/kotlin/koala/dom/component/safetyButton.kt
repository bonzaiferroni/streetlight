package koala.dom.component

import koala.SvgPack
import kampfire.model.Messenger
import kampfire.model.UIMessage
import koala.modifier.*
import kotlinx.html.BUTTON
import kotlinx.html.js.button
import koala.dom.AppendScope
import koala.dom.ViewScope
import koala.dom.asWeb
import koala.dom.onClick
import koala.component.configureButton

/**
 * A button that asks for a second click, showing [confirmText], before it calls [onConfirm].
 *
 * [onClick] receives whether the click was the confirming one.
 */
fun AppendScope.safetyButton(
    text: String,
    onConfirm: (() -> Unit)? = null,
    onClick: ((Boolean) -> Unit)?= null,
    mod: Modifier? = null,
    flair: SvgPack? = null,
    confirmText: String = "Confirm",
    block: BUTTON.() -> Unit = {},
) {
    var isConfirm = false

    val element = button {
        configureButton(text, modify(Zen, mod), flair, block)
    }.asWeb()

    element.onClick {
        onClick?.invoke(isConfirm)
        if (!isConfirm) {
            isConfirm = true
            element.unmodify(Zen)
            element.modify(Danger)
            element.textContent = confirmText
        } else {
            onConfirm?.invoke()
        }
    }
}

/**
 * A button that asks for a second click before it calls [onConfirm], delivering [message] to [messenger] on the
 * first.
 */
fun ViewScope.safetyButton(
    text: String,
    onConfirm: () -> Unit,
    messenger: Messenger,
    message: String = "This action cannot be reversed.",
    mod: Modifier? = null,
    block: BUTTON.() -> Unit = {},
) {
    safetyButton(text, onConfirm, onClick = { isConfirm ->
        if (!isConfirm) {
            messenger.deliver(UIMessage(message))
        }
    }, mod, confirmText = "I Understand", block = block)
}