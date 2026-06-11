package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.html.P
import kotlinx.html.js.p
import org.w3c.dom.HTMLParagraphElement

fun DOM.textBlock(
    text: String = "",
    mod: ModifierSet? = null,
    block: (P.() -> Unit)? = null
) = p {
    addModifiers(mod)
    +text
    block?.invoke(this)
}

fun <T> ScopedDOM.textBlock(
    binding: Flow<T>,
    mod: ModifierSet? = null,
    provideValue: (T) -> String = { it.toString() },
    block: (P.() -> Unit)? = null
): HTMLParagraphElement {
    val element = this@textBlock.textBlock(mod = mod, block = block)
    renderScope.launch {
        binding.distinctUntilChanged().collect {
            element.textContent = provideValue(it)
        }
    }
    return element
}