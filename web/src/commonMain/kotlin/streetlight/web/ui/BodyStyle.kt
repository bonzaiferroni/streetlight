package streetlight.web.ui

import koala.css.Class
import koala.css.Gap4
import koala.css.MarginTop1
import koala.css.OpacityHigh
import koala.css.TextUppercase
import koala.css.modify

object BodyStyle {
    val Column = modify(MarginTop1, Gap4)
    val LabelHeading = modify(TextUppercase, OpacityHigh)
    val FlexGrid2 = Class("form-row")
}

//language="CSS"
val BodyCss = with(BodyStyle) { """
$FlexGrid2 {
    display: flex;
    flex-wrap: wrap;
    gap: var(--unit-spacing-2);
    
    > * {
        flex: 1 1 400px;
        min-width: 0;
    }
}
"""}