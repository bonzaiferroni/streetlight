package streetlight.web.ui

import koala.core.queryAndInitLotties
import koala.dom.AppendScope
import koala.dom.column
import streetlight.model.data.ExtraLink
import streetlight.web.pages.configureAppFooter
import web.html.HTMLElement

fun AppendScope.appFooter(
    sourcePath: String,
    vararg additional: ExtraLink
): HTMLElement {
    val element = column {
        configureAppFooter(sourcePath, *additional)
    }

    queryAndInitLotties(element)
    return element
}