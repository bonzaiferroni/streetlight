package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.sectionHeading(
    labelContent: DIV.() -> Unit,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) {
    row(modify(mod, AlignItemsEnd)) {
        block()

//        spacer(modify(Width4))
        // icon(SvgFiles.EyeOff, modify(OpacitySome, Height4))
        row(modify(Flex1, JustifyContentCenter)) {
            labelContent()
        }
//        icon(SvgFile.EyeClosed, modify(OpacitySome, Height4))
    }
}

fun FlowContent.sectionHeading(
    label: String,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) {
    sectionHeading(
        labelContent = { centeredHeading(label, modify(Flex1)) },
        mod = mod,
        block = block,
    )
}