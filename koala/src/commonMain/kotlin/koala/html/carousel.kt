package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.FlowContent
import kotlinx.html.DIV
import kotlinx.html.div

fun FlowContent.carousel(modifiers: ModifierSet? = null, content: DIV.() -> Unit) {
    div {
        addModifiers(ElementClass.carousel, modifiers)
        row(block = content)
    }
}