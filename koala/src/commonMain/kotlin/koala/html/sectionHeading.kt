package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

/** A heading row with the label built by [labelContent] centered, after what [block] builds. */
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

/** A heading row with [label] centered, after what [block] builds. */
fun FlowContent.sectionHeading(
    label: String,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) {
    sectionHeading(
        labelContent = { centeredHeading(label, Flex1) },
        mod = mod,
        block = block,
    )
}