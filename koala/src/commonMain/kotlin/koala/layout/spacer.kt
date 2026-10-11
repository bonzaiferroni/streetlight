package koala.layout

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import koala.component.heading
import koala.component.heading3
import koala.component.hr
import koala.interop.invoke

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