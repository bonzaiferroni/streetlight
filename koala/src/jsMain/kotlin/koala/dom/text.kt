package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.html.EM
import kotlinx.html.P
import kotlinx.html.SPAN
import kotlinx.html.STRONG
import kotlinx.html.js.p
import kotlinx.html.js.em as emTag
import kotlinx.html.js.strong as strongTag
import kotlinx.html.js.span as spanTag
import org.w3c.dom.HTMLParagraphElement

fun AppendScope.textBlock(
    text: String = "",
    mod: ModifierSet? = null,
    block: (P.() -> Unit)? = null
) = p {
    addModifiers(mod)
    +text
    block?.invoke(this)
}

fun <T> ViewScope.textBlock(
    binding: Flow<T>,
    mod: ModifierSet? = null,
    provideValue: (T) -> String = { it.toString() },
    block: (P.() -> Unit)? = null
): HTMLParagraphElement {
    val element = this@textBlock.textBlock(mod = mod, block = block)
    contentScope.launch {
        binding.distinctUntilChanged().collect {
            element.textContent = provideValue(it)
        }
    }
    return element
}

fun AppendScope.em(
    text: String = "",
    mod: ModifierSet? = null,
    block: EM.() -> Unit = { }
) = emTag {
    addModifiers(mod)
    +text
    block()
}

fun AppendScope.strong(
    text: String = "",
    mod: ModifierSet? = null,
    block: STRONG.() -> Unit = { }
) = strongTag {
    addModifiers(mod)
    +text
    block()
}

fun AppendScope.span(
    text: String = "",
    mod: ModifierSet? = null,
    block: SPAN.() -> Unit = { }
) = spanTag {
    addModifiers(mod)
    +text
    block()
}