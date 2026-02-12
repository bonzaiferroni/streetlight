package koala.dom

import koala.css.ModifierSet
import koala.css.applyModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.html.P
import kotlinx.html.js.p
import org.w3c.dom.HTMLParagraphElement

fun DOMContext.textBlock(
    text: String = "",
    modifiers: ModifierSet? = null,
    block: (P.() -> Unit)? = null
) = p {
    applyModifiers(modifiers)
    +text
    block?.invoke(this)
}

fun RenderContext.textBlock(
    flow: Flow<String>,
    modifiers: ModifierSet? = null,
    block: (P.() -> Unit)? = null
): HTMLParagraphElement {
    val element = this@textBlock.textBlock(modifiers = modifiers, block = block)
    renderScope.launch {
        flow.distinctUntilChanged().collect {
            element.textContent = it
        }
    }
    return element
}