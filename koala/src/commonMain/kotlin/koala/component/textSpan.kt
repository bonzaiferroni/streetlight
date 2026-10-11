package koala.component

import koala.modifier.*
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.span
import kotlinx.html.SPAN
import koala.interop.invoke

/** [text] in a span. */
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