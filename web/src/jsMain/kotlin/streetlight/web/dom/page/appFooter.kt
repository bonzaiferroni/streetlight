package streetlight.web.dom.page

import koala.core.queryAndInitLotties
import koala.dom.AppendScope
import koala.dom.layout.column
import streetlight.model.data.ExtraLink
import streetlight.web.page.configureAppFooter
import web.html.HTMLElement
import streetlight.web.utility.element

/** Appends an app footer and starts its animation. */
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