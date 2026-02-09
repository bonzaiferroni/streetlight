package koala.css

import kotlinx.css.*

object Gap0: CssClass { override val value = "gap-0" }
object Gap1: CssClass { override val value = "gap-1" }
object Gap2: CssClass { override val value = "gap-2" }
object Gap4: CssClass { override val value = "gap-4" }
object Gap8: CssClass { override val value = "gap-8" }
object Flex1: CssClass { override val value = "flex-1" }
object Flex2: CssClass { override val value = "flex-2" }
object Flex3: CssClass { override val value = "flex-3" }
object Flex4: CssClass { override val value = "flex-4" }
object AlignItemsCenter: CssClass { override val value = "align-items-center" }
object AlignItemsStretch: CssClass { override val value = "align-items-stretch" }
object AlignItemsStart: CssClass { override val value = "align-items-start" }
object AlignItemsEnd: CssClass { override val value = "align-items-end" }
object JustifyCenter: CssClass { override val value = "justify-content-center" }
object JustifySpaceAround: CssClass { override val value = "justify-content-space-around" }
object Width100: CssClass { override val value = "width-100" }
object Width2: CssClass { override val value = "width-2" }
object Width4: CssClass { override val value = "width-4" }
object Height100: CssClass { override val value = "height-100" }
object Height2: CssClass { override val value = "height-2" }
object Height3: CssClass { override val value = "height-3" }
object Height4: CssClass { override val value = "height-4" }
object Height32: CssClass { override val value = "height-32" }
object Height48: CssClass { override val value = "height-48" }
object Size100: CssClass { override val value = "size-100" }
object FillHeight: CssClass { override val value = "fill-height" }
object FlexItems1: CssClass { override val value = "flex-items-1" }
object FlexItemsBasis50: CssClass { override val value = "flex-items-basis-50" }
object TextAlignCenter: CssClass { override val value = "text-align-center"}
object TextAlignRight: CssClass { override val value = "text-align-right"}
object RowReverse: CssClass { override val value = "row-reverse" }
object QueryRowReverse: CssClass { override val value = "query-row-reverse" }
object MaxWidth25: CssClass { override val value = "max-width-25" }
object MaxWidth50: CssClass { override val value = "max-width-50" }
object MarginAuto: CssClass { override val value = "margin-auto" }
object MarginTop4: CssClass { override val value = "margin-top-4" }
object MarginLeft1: CssClass { override val value = "margin-left-1" }
object SpaceBetween: CssClass { override val value = "space-between" }
object NoWrap: CssClass { override val value = "no-wrap" }
object Square: CssClass { override val value = "square" }
object StackChildren: CssClass { override val value = "stack-children" }
object Start: CssClass { override val value = "start" }
object End: CssClass { override val value = "end" }
object Center: CssClass { override val value = "center" }

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
        height = 2.rem
    }

    rule(Height3) {
        height = 3.rem
    }

    rule(Height4) {
        height = 4.rem
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