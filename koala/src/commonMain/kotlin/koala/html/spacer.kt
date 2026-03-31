package koala.html

import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.spacer(
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null,
) {
    div {
        addModifiers(modifiers)
        block?.invoke(this)
    }
}

fun FlowContent.centeredHeading(
    text: String,
    modifiers: ModifierSet? = null,
) {
    row(modify(modifiers, JustifyContentCenter, AlignItemsCenter)) {
//        hr {
//            addModifiers(modify(Flex1, OpacitySome))
//        }
        heading3(text, modify(OpacityMost, LineHeight1, WhiteSpaceNoWrap))
//        hr {
//            addModifiers(modify(Flex1, OpacitySome))
//        }
    }
}