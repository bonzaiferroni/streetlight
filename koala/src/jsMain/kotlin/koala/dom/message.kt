package koala.dom

import koala.css.ModifierSet
import koala.css.applyModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.js.p
import kotlinx.html.style

fun RenderContext.message(
    flow: Flow<UIMessage?>,
    modifiers: ModifierSet? = null
) {
    val element = p {
        applyModifiers(modifiers)
    }

    renderScope.launch {
        flow.collect { msg ->
            element.textContent = msg?.message
        }
    }
}