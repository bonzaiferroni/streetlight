package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import kotlinx.html.FlowContent
import kotlinx.html.img

fun FlowContent.headerOf(
    text: String,
    src: String?,
    modifiers: ModifierSet? = null
) {
    box {
        applyModifiers(ElementClass.headerImage, modifiers)

        src?.let {
            img {
                this.src = src
            }
        }

        heading1(text)
    }
}