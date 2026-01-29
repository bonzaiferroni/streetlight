package koala.css

import kotlinx.css.Align
import kotlinx.css.CssBuilder
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.Padding
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.gap
import kotlinx.css.padding

object Column : CssClass { override val value = "column" }
object Row : CssClass { override val value = "row" }
object QueryRow: CssClass { override val value = "query-row" }
object Card : CssClass { override val value = "card" }
object Box : CssClass { override val value = "box"}

fun CssBuilder.layout(theme: KoalaTheme) {
    // layouts
    rule(Column) {
        display = Display.flex
        flexDirection = FlexDirection.column
        gap = theme.spacingUnit
    }

    rule(Row) {
        display = Display.flex
        flexDirection = FlexDirection.row
        gap = theme.spacingUnit
        alignItems = Align.center
    }

    rule(Card) {
        display = Display.flex
        flexDirection = FlexDirection.column
        gap = theme.spacingUnit
        borderRadius = theme.spacingUnit
        backgroundColor = theme.void.changeAlpha(0.2)
        padding = Padding(theme.spacingUnit)
    }

    rule(Box) {
    }
}