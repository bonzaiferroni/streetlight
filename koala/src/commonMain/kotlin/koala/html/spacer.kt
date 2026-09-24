package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

/** An empty element, sized by [mod], that takes up space. */
fun FlowContent.spacer(
    mod: Modifier? = null,
    block: (DIV.() -> Unit)? = null,
) {
    div {
        addModifiers(mod)
        block?.invoke(this)
    }
}

/** [text] as a centered heading. */
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