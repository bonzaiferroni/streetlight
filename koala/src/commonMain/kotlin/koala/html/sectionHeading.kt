package koala.html

import koala.SvgFile
import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.sectionHeading(
    labelContent: DIV.() -> Unit,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    row(modify(modifiers, AlignItemsEnd)) {
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
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    sectionHeading(
        labelContent = { centeredHeading(label, modify(Flex1)) },
        modifiers = modifiers,
        block = block,
    )
}