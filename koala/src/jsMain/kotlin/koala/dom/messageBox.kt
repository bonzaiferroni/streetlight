package koala.dom

import koala.css.*
import koala.html.MessageBoxKey
import koala.model.Store
import kotlinx.coroutines.flow.Flow

fun RenderScope.messageBox(
    flow: Flow<UIMessage?>,
    modifiers: ModifierSet? = null,
) {
    flowBlock(flow, modifiers) { message ->
        val message = message ?: return@flowBlock
        card(modify(modifiers, MessageBoxKey.Class, MoonShadow)) {
            column {
                val paragraphs = message.text.split("\n\n")
                paragraphs.forEach { text ->
                    textBlock(text)
                }
            }
        }
    }
}

fun RenderScope.messageBox(
    store: Store<UIMessage?>,
    modifiers: ModifierSet? = null,
) = messageBox(store.flow, modifiers)