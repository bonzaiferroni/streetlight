package koala.dom

import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.css.*
import koala.html.MessageBox
import koala.model.Tap

fun ViewScope.messageBox(
    tap: Tap<UIMessage?>,
    modifiers: ModifierSet? = null,
) {
    flowBlock(tap, modifiers) { message ->
        val message = message ?: return@flowBlock
        val typeMod = message.messageType.toModifier()
        card(modify(modifiers, MessageBox.Mod, MoonShadow, typeMod)) {
            textBlock(message.text)
        }
    }
}

// fun ViewScope.messageBox(
//     store: Store<UIMessage?>,
//     modifiers: ModifierSet? = null,
// ) = messageBox(store, modifiers)

fun UIMessageType.toModifier() = when (this) {
    UIMessageType.Error -> MessageBox.Error
    UIMessageType.Success -> MessageBox.Success
    else -> null
}