package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.spacer(
    mod: Modifier? = null,
    block: (DIV.() -> Unit)? = null,
) {
    div {
        addModifiers(mod)
        block?.invoke(this)
    }
}

fun FlowContent.centeredHeading(
    text: String,
    mod: Modifier? = null,
) {
    row(modify(mod, JustifyContentCenter, AlignItemsCenter)) {
//        hr {
//            addModifiers(modify(Flex1, OpacitySome))
//        }
        heading3(text, modify(OpacityHigh, LineHeight1, WhiteSpaceNoWrap))
//        hr {
//            addModifiers(modify(Flex1, OpacitySome))
//        }
    }
}