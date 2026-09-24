package streetlight.web.ui

import koala.dom.AppendScope
import koala.dom.column
import koala.modifier.*
import kotlinx.html.DIV
import streetlight.web.pages.appHeader

fun AppendScope.shellBody(
    sourceFile: String,
    mod: Modifier? = null,
    content: DIV.() -> Unit
) = column(modify(BodyStyle.ShellColumn, mod)) {
    appHeader()
    column(BodyStyle.MainColumn) {
        content()
        appFooter(sourcePathJsUi(sourceFile))
    }
}