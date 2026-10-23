package streetlight.web.layout

import koala.component.column
import koala.component.row
import koala.component.section
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.SECTION
import streetlight.web.page.appFooter
import streetlight.web.page.appHeader

/** The page of a shell: the [appHeader], then [content] in a body section closed by the footer. */
fun FlowContent.mainBody(
    sourceFile: String,
    mod: Modifier? = null,
    header: (DIV.() -> Unit)? = { appHeader() },
    content: SECTION.() -> Unit
) = pageBody(sourcePathShell(sourceFile), mod, header, content)

/**
 * The shape every body shares: what [header] builds, then [content] in a body section closed by a footer linking
 * [sourcePath].
 */
fun FlowContent.pageBody(
    sourcePath: String,
    mod: Modifier?,
    header: (DIV.() -> Unit)?,
    content: SECTION.() -> Unit,
) {
    column(modify(BodyStyle.ShellColumn, mod)) {
        header?.invoke(this)
        section(BodyStyle.MainColumn) {
            content()
            appFooter(sourcePath)
        }
    }
}

fun FlowContent.columnRow(
    mod: Modifier? = null,
    content: DIV.() -> Unit,
) {
    row(modify(BodyStyle.ColumnRow, mod)) {
        content()
    }
}

object BodyStyle {
    val ShellColumn = modify(Gap0)
    val MainColumn = modify(Gap(4))
    val ZenColumn = modify(Gap(8))
    val ColumnRow = Class("column-row")
}

//language="CSS"
val BodyCss = with(BodyStyle) { """
$ColumnRow {
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

fun sourcePathView(filename: String) = "web/src/jsMain/kotlin/streetlight/web/view/${filename}"
fun sourcePathShell(filename: String) = "web/src/commonMain/kotlin/streetlight/web/shell/${filename}"
