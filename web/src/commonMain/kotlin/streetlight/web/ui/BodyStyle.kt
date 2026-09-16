package streetlight.web.ui

import koala.modifier.Class
import koala.modifier.Gap0
import koala.modifier.Gap4
import koala.modifier.modify

object BodyStyle {
    val ShellColumn = modify(Gap0)
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