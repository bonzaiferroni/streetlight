package koala.css

import kotlinx.css.CssBuilder
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.flexDirection
import kotlinx.css.justifyContent

fun CssBuilder.queryLayout(theme: KoalaTheme) {

    media("(min-width: 600px)") {
        rule(QueryRow) {
            flexDirection = FlexDirection.row
        }

        rule(QueryRowReverse) {
            flexDirection = FlexDirection.rowReverse
            justifyContent = JustifyContent.flexEnd
        }
    }
}