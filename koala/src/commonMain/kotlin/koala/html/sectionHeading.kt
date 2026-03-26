package koala.html

import koala.SvgFiles
import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.SECTION

fun FlowContent.sectionHeading(
    label: String,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    row(modify(modifiers, Height5, AlignItemsCenter, MarginTop8)) {
        block()

        icon(SvgFiles.EyeOff, modify(OpacitySome, Height4))
        centeredHeading(label, modify(Flex1))
        icon(SvgFiles.EyeClosed, modify(OpacitySome, Height4))
    }
}

