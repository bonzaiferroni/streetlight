package streetlight.web.ui

import koala.dom.AppendScope
import koala.dom.column
import koala.html.section
import koala.modifier.*
import kotlinx.html.SECTION
import streetlight.web.pages.appHeader

fun AppendScope.mainBody(
    sourceFile: String,
    mod: Modifier? = null,
    content: SECTION.() -> Unit
) = column(modify(BodyStyle.ShellColumn, mod)) {
    appHeader()
    section(BodyStyle.MainColumn) {
        content()
        appFooter(sourcePathJsUi(sourceFile))
    }
}