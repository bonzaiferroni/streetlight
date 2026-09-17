package streetlight.web.ui

import koala.modifier.*

object BodyStyle {
    val ShellColumn = modify(Gap0)
    val MainColumn = modify(Gap(4))
    val FormRow = Class("form-row")
}

//language="CSS"
val BodyCss = with(BodyStyle) { """
$FormRow {
    display: flex;
    flex-wrap: wrap;
    gap: var(--unit-2);
    justify-content: center;
    
    > * {
        flex: 1 1 400px;
        min-width: 0;
        max-width: 540px;
    }
}
"""}