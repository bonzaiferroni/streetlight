package streetlight.web.ui

import koala.css.Class
import koala.css.OpacityHigh
import koala.css.TextSmall
import koala.css.modify

object Form {
    val Row = Class("form-row")

    val bulletsMod = modify(OpacityHigh, TextSmall)
}

//language="CSS"
val FormCss = with(Form) { """
$Row {
    display: flex;
    flex-wrap: wrap;
    gap: var(--unit-spacing-2);
    
    > * {
        flex: 1 1 400px;
        min-width: 0;
    }
}
"""}