package koala.dom

import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import koala.css.*
import koala.html.MessageBox
import koala.model.Store
import kotlinx.coroutines.flow.Flow

fun AppScope.messageBox(
    flow: Flow<UIMessage?>,
    modifiers: ModifierSet? = null,
) {
    flowBlock(flow, modifiers) { message ->
        val message = message ?: return@flowBlock
        val typeMod = when (message.messageType) {
            UIMessageType.Error -> MessageBox.Error
            UIMessageType.Success -> MessageBox.Success
            else -> null
        }
        card(modify(modifiers, MessageBox.Class, MoonShadow, typeMod)) {
            textBlock(message.text)
        }
    }
}

fun AppScope.messageBox(
    store: Store<UIMessage?>,
    modifiers: ModifierSet? = null,
) = messageBox(store.flow, modifiers)