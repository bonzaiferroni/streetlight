package streetlight.web.ui

import koala.css.Class
import koala.css.Gap4
import koala.css.modify

object BodyStyle {
    val MainColumn = modify(Gap4)
    val FormRow = Class("form-row")
}

//language="CSS"
val BodyCss = with(BodyStyle) { """
$FormRow {
    display: flex;
    flex-wrap: wrap;
    gap: var(--unit-spacing-2);
    justify-content: center;
    
    > * {
        flex: 1 1 400px;
        min-width: 0;
        max-width: 540px;
    }
}
"""}