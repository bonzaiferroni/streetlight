package koala.dom

import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.js.p

fun RenderContext.messageBox(
    flow: Flow<UIMessage?>,
    modifiers: ModifierSet? = null,
    animate: Boolean = true
) {
    flowBlock(flow, modifiers, animate) { message ->
        val text = message?.text
        if (text != null) {
            textBlock(text)
        }
    }
}