package koala.html

import koala.modifier.*
import kotlinx.html.FlowContent
import kotlinx.html.P
import kotlinx.html.p

/** [content] as small, faded text. */
fun FlowContent.textLabel(
    content: String,
    mod: Modifier? = null,
    block: (P.() -> Unit) = { }
) {
    p {
        addModifiers(TextLabelKey.Class, mod)
        block()
        +content
    }
}

object TextLabelKey {
    val Class = Class("text-label")
}

val TextLabelCss get() = with(TextLabelKey) { """
$Class {
    font-size: 0.8rem;
    opacity: 0.5;
}
""" }