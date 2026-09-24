package streetlight.web.ui

import koala.html.column
import koala.html.section
import koala.modifier.*
import kotlinx.html.FlowContent
import kotlinx.html.SECTION
import streetlight.web.pages.appFooter
import streetlight.web.pages.appHeader

fun FlowContent.mainBody(
    sourceFile: String,
    mod: Modifier? = null,
    content: SECTION.() -> Unit
) {
    column(modify(BodyStyle.ShellColumn, mod)) {
        appHeader()
        section(BodyStyle.MainColumn) {
            content()
            appFooter(sourcePathShells(sourceFile))
        }
    }
}

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

fun sourcePathJsUi(filename: String) = "web/src/jsMain/kotlin/streetlight/web/ui/${filename}"
fun sourcePathCommonUi(filename: String) = "web/src/commonMain/kotlin/streetlight/web/ui/${filename}"
fun sourcePathShells(filename: String) = "web/src/commonMain/kotlin/streetlight/web/shells/${filename}"
