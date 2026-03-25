package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.setModifiers
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.img

fun FlowContent.headerOf(
    text: String,
    src: String?,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    box {
        setModifiers(ElementClass.headerImage, modifiers)
        block()

        src?.let {
            img {
                this.src = src
            }
        }

        heading1(text)
    }
}