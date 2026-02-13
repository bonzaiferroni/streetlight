package koala.css

import kotlinx.css.*

object Gap0: Modifier { override val value = "gap-0" }
object Gap1: Modifier { override val value = "gap-1" }
object Gap2: Modifier { override val value = "gap-2" }
object Gap4: Modifier { override val value = "gap-4" }
object Gap8: Modifier { override val value = "gap-8" }
object Flex1: Modifier { override val value = "flex-1" }
object Flex2: Modifier { override val value = "flex-2" }
object Flex3: Modifier { override val value = "flex-3" }
object Flex4: Modifier { override val value = "flex-4" }
object AlignItemsCenter: Modifier { override val value = "align-items-center" }
object AlignItemsStretch: Modifier { override val value = "align-items-stretch" }
object AlignItemsStart: Modifier { override val value = "align-items-start" }
object AlignItemsEnd: Modifier { override val value = "align-items-end" }
object JustifyCenter: Modifier { override val value = "justify-content-center" }
object JustifySpaceAround: Modifier { override val value = "justify-content-space-around" }
object Width100: Modifier { override val value = "width-100" }
object Width2: Modifier { override val value = "width-2" }
object Width4: Modifier { override val value = "width-4" }
object Width8: Modifier { override val value = "width-8" }
object Width16: Modifier { override val value = "width-16" }
object Width24: Modifier { override val value = "width-24" }
object Width32: Modifier { override val value = "width-32" }
object Width64: Modifier { override val value = "width-64" }
object Height100: Modifier { override val value = "height-100" }
object Height2: Modifier { override val value = "height-2" }
object Height3: Modifier { override val value = "height-3" }
object Height4: Modifier { override val value = "height-4" }
object Height6: Modifier { override val value = "height-6" }
object Height8: Modifier { override val value = "height-8" }
object Height16: Modifier { override val value = "height-16" }
object Height32: Modifier { override val value = "height-32" }
object Height48: Modifier { override val value = "height-48" }
object MinHeight4: Modifier { override val value = "min-height-4" }
object MinHeight8: Modifier { override val value = "min-height-8" }
object Size100: Modifier { override val value = "size-100" }
object FillHeight: Modifier { override val value = "fill-height" }
object FlexItems1: Modifier { override val value = "flex-items-1" }
object FlexItemsBasis50: Modifier { override val value = "flex-items-basis-50" }
object TextAlignCenter: Modifier { override val value = "text-align-center"}
object TextAlignRight: Modifier { override val value = "text-align-right"}
object RowReverse: Modifier { override val value = "row-reverse" }
object QueryRowReverse: Modifier { override val value = "query-row-reverse" }
object MaxWidth25: Modifier { override val value = "max-width-25" }
object MaxWidth50: Modifier { override val value = "max-width-50" }
object MarginAuto: Modifier { override val value = "margin-auto" }
object MarginTop4: Modifier { override val value = "margin-top-4" }
object MarginLeft1: Modifier { override val value = "margin-left-1" }
object SpaceBetween: Modifier { override val value = "space-between" }
object NoWrap: Modifier { override val value = "no-wrap" }
object WrapFlex: Modifier { override val value = "wrap-flex" }
object Square: Modifier { override val value = "square" }
object StackChildren: Modifier { override val value = "stack-children" }
object Start: Modifier { override val value = "start" }
object End: Modifier { override val value = "end" }
object Center: Modifier { override val value = "center" }

fun CssBuilder.layoutUtilities(theme: KoalaTheme) {
    rule(Gap0) {
        gap = theme.spacingUnit * 0
    }

    rule(Gap1) {
        gap = theme.spacingUnit * 1
    }

    rule(Gap2) {
        gap = theme.spacingUnit * 2
    }

    rule(Gap4) {
        gap = theme.spacingUnit * 4
    }

    rule(Gap8) {
        gap = theme.spacingUnit * 8
    }

    rule(Flex1) {
        flex = Flex(1)
    }

    rule(Flex2) {
        flex = Flex(2)
    }

    rule(Flex3) {
        flex = Flex(3)
    }

    rule(Flex4) {
        flex = Flex(4)
    }

    rule(AlignItemsCenter) {
        alignItems = Align.center
    }

    rule(AlignItemsStretch) {
        alignItems = Align.stretch
    }

    rule(AlignItemsStart) {
        alignItems = Align.flexStart
    }

    rule(AlignItemsEnd) {
        alignItems = Align.flexEnd
    }

    rule(JustifyCenter) {
        justifyContent = JustifyContent.center
    }

    rule(JustifySpaceAround) {
        justifyContent = JustifyContent.spaceAround
    }

    rule(Width100) {
        width = 100.pct
    }

    rule(Width2) {
        width = theme.spacingUnit * 2
    }

    rule(Width4) {
        width = theme.spacingUnit * 4
    }

    rule(FlexItems1) {
        children {
            flex = Flex(1)
        }
    }

    rule(TextAlignCenter) {
        textAlign = TextAlign.center
    }

    rule(TextAlignRight) {
        textAlign = TextAlign.right
    }

    rule(RowReverse) {
        flexDirection = FlexDirection.rowReverse
    }

    rule(MaxWidth25) {
        maxWidth = 25.pct
    }

    rule(MaxWidth50) {
        maxWidth = 50.pct
    }

    rule(MarginAuto) {
        margin = Margin(LinearDimension.auto)
    }

    rule(MarginTop4) {
        marginTop = theme.spacingUnit * 4
    }

    rule(SpaceBetween) {
        justifyContent = JustifyContent.spaceBetween
    }

    rule(Height100) {
        height = 100.pct
    }

    rule(Height2) {
        height = theme.spacingUnit * 2
    }

    rule(Height3) {
        height = theme.spacingUnit * 3
    }

    rule(Height4) {
        height = theme.spacingUnit * 4
    }

    rule(FillHeight) {
        height = LinearDimension.auto
        maxHeight = 100.pct
    }

    rule(NoWrap) {
        flexWrap = FlexWrap.nowrap
        whiteSpace = WhiteSpace.nowrap
    }
}