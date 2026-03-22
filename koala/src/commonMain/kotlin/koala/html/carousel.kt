package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.html.FlowContent
import kotlinx.html.DIV
import kotlinx.html.div

fun FlowContent.carousel(modifiers: ModifierSet? = null, content: DIV.() -> Unit) {
    div {
        applyModifiers(ElementClass.carousel, modifiers)
        row(content = content)
    }
}