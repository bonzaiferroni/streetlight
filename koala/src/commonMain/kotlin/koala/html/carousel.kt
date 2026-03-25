package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.setModifiers
import kotlinx.html.FlowContent
import kotlinx.html.DIV
import kotlinx.html.div

fun FlowContent.carousel(modifiers: ModifierSet? = null, content: DIV.() -> Unit) {
    div {
        setModifiers(ElementClass.carousel, modifiers)
        row(block = content)
    }
}