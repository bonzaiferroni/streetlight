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
object Width100: CssClass { override val value = "width-100" }
object FlexItems1: CssClass { override val value = "flex-items-1" }
object TextAlignCenter: CssClass { override val value = "text-align-center"}
object TextAlignRight: CssClass { override val value = "text-align-right"}
object RowReverse: CssClass { override val value = "row-reverse" }
object QueryRowReverse: CssClass { override val value = "query-row-reverse" }
object MaxWidth25: CssClass { override val value = "max-width-25" }
object MaxWidth50: CssClass { override val value = "max-width-50" }
object MarginAuto: CssClass { override val value = "margin-auto" }
object MarginTop4: CssClass { override val value = "margin-top-4" }

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

    rule(Width100) {
        width = 100.pct
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
}