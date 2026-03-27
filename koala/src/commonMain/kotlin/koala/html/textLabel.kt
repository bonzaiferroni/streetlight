package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.FlowContent
import kotlinx.html.P
import kotlinx.html.p

fun FlowContent.textLabel(
    content: String,
    modifiers: ModifierSet? = null,
    block: (P.() -> Unit) = { }
) {
    p {
        addModifiers(TextLabelKey.Class, modifiers)
        block()
        +content
    }
}

object TextLabelKey {
    val Class = Css("text-label")
}

val TextLabelCss get() = """
.text-label {
    font-size: 0.8rem;
    opacity: 0.5;
}
"""