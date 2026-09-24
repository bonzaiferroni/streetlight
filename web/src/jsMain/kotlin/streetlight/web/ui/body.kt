package streetlight.web.ui

import koala.dom.AppendScope
import koala.dom.column
import koala.html.section
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.SECTION
import streetlight.web.pages.appHeader
import streetlight.web.pages.configHeader

/** The page of a view: the [appHeader], then [content] in a body section closed by the footer. */
fun AppendScope.mainBody(
    sourceFile: String,
    mod: Modifier? = null,
    content: SECTION.() -> Unit
) = pageBody(sourcePathJsUi(sourceFile), mod, { appHeader() }, content)

/**
 * The page of a config view: a [configHeader] of [titleFirst] and [titleSecond] in place of the app header, then
 * [content] in a body section closed by the footer.
 */
fun AppendScope.configBody(
    titleFirst: String,
    titleSecond: String,
    sourceFile: String,
    mod: Modifier? = null,
    content: SECTION.() -> Unit
) = pageBody(sourcePathJsUi(sourceFile), mod, { configHeader(titleFirst, titleSecond) }, content)

/**
 * The shape every body shares: what [header] builds, then [content] in a body section closed by a footer linking
 * [sourcePath].
 */
fun AppendScope.pageBody(
    sourcePath: String,
    mod: Modifier?,
    header: DIV.() -> Unit,
    content: SECTION.() -> Unit,
) = column(modify(BodyStyle.ShellColumn, mod)) {
    header()
    section(BodyStyle.MainColumn) {
        content()
        appFooter(sourcePath)
    }
}
