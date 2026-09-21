package koala.dom

import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.modifier.*
import koala.html.MessageBox
import kampfire.model.Tap

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

fun UIMessageType.toModifier() = when (this) {
    UIMessageType.Error -> MessageBox.Error
    UIMessageType.Success -> MessageBox.Success
    else -> null
}