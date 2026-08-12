package koala.dom

import kampfire.model.Messenger
import kampfire.model.UIMessage
import koala.css.Danger
import koala.css.ModifierSet
import koala.css.Zen
import koala.css.modify
import koala.html.configureButton
import kotlinx.html.BUTTON
import kotlinx.html.js.button

fun TagScope.safetyButton(
    text: String,
    onConfirm: (() -> Unit)? = null,
    onClick: ((Boolean) -> Unit)?= null,
    mod: ModifierSet? = null,
    flair: String? = null,
    confirmText: String = "Confirm",
    block: BUTTON.() -> Unit = {},
) {
    var isConfirm = false

    val element = button {
        configureButton(text, modify(Zen, mod), flair, block)
    }

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

fun ViewScope.safetyButton(
    text: String,
    onConfirm: () -> Unit,
    messenger: Messenger,
    message: String = "This action cannot be reversed.",
    mod: ModifierSet? = null,
    block: BUTTON.() -> Unit = {},
) {
    safetyButton(text, onConfirm, onClick = { isConfirm ->
        if (!isConfirm) {
            messenger.deliver(UIMessage(message))
        }
    }, mod, confirmText = "I Understand", block = block)
}