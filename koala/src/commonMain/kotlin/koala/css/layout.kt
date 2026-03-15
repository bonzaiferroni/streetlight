package koala.css

import kotlinx.css.Align
import kotlinx.css.CssBuilder
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.gap

object Column : Modifier { override val value = "column" }
object Row : Modifier { override val value = "row" }
object QueryRowMedium: Modifier { override val value = "query-row-medium" }
object QueryRowLarge: Modifier { override val value = "query-row-large" }
object Card : Modifier { override val value = "card" }
object Box : Modifier { override val value = "box"}

fun CssBuilder.layout(theme: KoalaTheme) {
    // layouts
    rule(Column) {
        display = Display.flex
        flexDirection = FlexDirection.column
        gap = theme.spacingUnit
        alignItems = Align.start
    }

    rule(Row) {
        display = Display.flex
        flexDirection = FlexDirection.row
        gap = theme.spacingUnit
        alignItems = Align.center
    }

//    rule(Card) {
//        display = Display.flex
//        flexDirection = FlexDirection.column
//        gap = theme.spacingUnit
//        borderRadius = theme.spacingUnit
//        backgroundColor = theme.void.changeAlpha(0.2)
//        padding = Padding(theme.spacingUnit)
//    }

    rule(Box) {
    }
}