package koala.dom

import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.modifier.*
import koala.html.MessageBox
import kampfire.model.Tap

/** The message of [tap] in a card styled by its type, or nothing while it is `null`. */
fun ViewScope.messageBox(
    tap: Tap<UIMessage?>,
    mod: Modifier? = null,
) {
    flowBlock(tap, mod) { message ->
        val message = message ?: return@flowBlock
        val typeMod = message.messageType.toModifier()
        card(modify(mod, MessageBox.Mod, MoonShadow, typeMod)) {
            textBlock(message.text)
        }
    }
}

// fun ViewScope.messageBox(
//     store: Store<UIMessage?>,
//     mod: Modifier? = null,
// ) = messageBox(store, mod)

/** The class that styles a [messageBox] for this type, or `null` for a plain message. */
fun UIMessageType.toModifier() = when (this) {
    UIMessageType.Error -> MessageBox.Error
    UIMessageType.Success -> MessageBox.Success
    else -> null
}