package streetlight.web.dom.layout

import koala.dom.AppendScope
import koala.dom.component.column
import koala.component.section
import koala.model.WebRoute
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.SECTION
import streetlight.model.ui.HomeRoute
import streetlight.web.page.topBar
import streetlight.model.ui.TopBar
import streetlight.web.layout.BodyStyle
import streetlight.web.dom.page.appFooter
import streetlight.web.layout.mainBody
import streetlight.web.layout.pageBody
import streetlight.web.layout.sourcePathView

/** The page of a view: the logo's [topBar], then [content] in a body section closed by the footer. */
fun AppendScope.mainBody(
    sourceFile: String,
    mod: Modifier? = null,
    content: SECTION.() -> Unit
) = pageBody(sourcePathView(sourceFile), mod, { topBar(TopBar.Logo) }, content)

/**
 * The page of a config view: a two-tone top bar of [titleFirst] and [titleSecond] in place of the app header, then
 * [content] in a body section closed by the footer.
 */
fun AppendScope.configBody(
    titleFirst: String,
    titleSecond: String,
    sourceFile: String,
    route: WebRoute = HomeRoute(),
    mod: Modifier? = null,
    content: SECTION.() -> Unit
) = pageBody(sourcePathView(sourceFile), mod, { topBar(TopBar.TwoTone(titleFirst, titleSecond), route = route) }, content)

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
