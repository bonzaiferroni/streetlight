package koala.html

import koala.SvgFiles
import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.SECTION

fun FlowContent.sectionHeading(
    labelContent: DIV.() -> Unit,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    row(modify(modifiers, AlignItemsEnd, MarginTop4)) {
        block()

        icon(SvgFiles.EyeOff, modify(OpacitySome, Height4))
        row(modify(Flex1, JustifyContentCenter)) {
            labelContent()
        }
        icon(SvgFiles.EyeClosed, modify(OpacitySome, Height4))
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