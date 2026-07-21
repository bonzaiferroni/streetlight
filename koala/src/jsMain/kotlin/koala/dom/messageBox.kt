package koala.dom

import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.css.*
import koala.html.MessageBox
import koala.model.Field

fun ViewScope.messageBox(
    flow: Field<UIMessage?>,
    modifiers: ModifierSet? = null,
) {
    flowBlock(flow, modifiers) { message ->
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