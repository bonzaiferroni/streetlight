package koala.html

import koala.modifier.*
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.span
import kotlinx.html.SPAN

fun FlowOrPhrasingContent.textSpan(
    text: String,
    mod: Modifier? = null,
    block: (SPAN.() -> Unit)? = null,
) {
    span {
        addModifiers(mod)
        +text
        block?.invoke(this)
    }
}