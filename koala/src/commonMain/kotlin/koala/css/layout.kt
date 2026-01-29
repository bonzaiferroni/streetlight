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

fun CssBuilder.layout(theme: KoalaTheme) {
    // layouts
    classRule(Column) {
        display = Display.flex
        flexDirection = FlexDirection.column
        gap = theme.spacingUnit
    }

    classRule(Row) {
        display = Display.flex
        flexDirection = FlexDirection.row
        gap = theme.spacingUnit
        alignItems = Align.center
    }

    classRule(Card) {
        display = Display.flex
        flexDirection = FlexDirection.column
        gap = theme.spacingUnit
        borderRadius = theme.spacingUnit
        backgroundColor = theme.void.changeAlpha(0.2)
        padding = Padding(theme.spacingUnit)
    }

    classRule(Box) {
    }
}