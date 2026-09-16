package koala.html

import koala.modifier.ModifierSet
import koala.modifier.addModifiers
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.span
import kotlinx.html.SPAN

fun FlowOrPhrasingContent.textSpan(
    text: String,
    modifierSet: ModifierSet? = null,
    block: (SPAN.() -> Unit)? = null,
) {
    span {
        addModifiers(modifierSet)
        +text
        block?.invoke(this)
    }
}