package koala.css

import kotlinx.css.Align
import kotlinx.css.CssBuilder
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.gap

object Column : Modifier { override val identifier = "column" }
object Row : Modifier { override val identifier = "row" }
object QueryMediumRow: Modifier { override val identifier = "query-medium-row" }
object QueryMediumColumn: Modifier { override val identifier = "query-medium-column" }
object QueryMediumFlex1: Modifier { override val identifier = "query-medium-flex-1" }
object QueryMediumFlex2: Modifier { override val identifier = "query-medium-flex-2" }
object QueryMediumFlex3: Modifier { override val identifier = "query-medium-flex-3" }
object QueryMediumFlex4: Modifier { override val identifier = "query-medium-flex-4" }
object QueryLargeRow: Modifier { override val identifier = "query-large-row" }
object QueryLargeColumn: Modifier { override val identifier = "query-large-column" }
object QueryLargeFlex1: Modifier { override val identifier = "query-large-flex-1" }
object QueryLargeFlex2: Modifier { override val identifier = "query-large-flex-2" }
object QueryLargeFlex3: Modifier { override val identifier = "query-large-flex-3" }
object QueryLargeFlex4: Modifier { override val identifier = "query-large-flex-4" }
object Card : Modifier { override val identifier = "card" }
object Box : Modifier { override val identifier = "box"}

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