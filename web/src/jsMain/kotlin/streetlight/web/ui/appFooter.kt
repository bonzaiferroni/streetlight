package streetlight.web.ui

import koala.core.queryAndInitLotties
import koala.dom.TagScope
import koala.dom.column
import org.w3c.dom.HTMLElement
import streetlight.model.data.ExtraLink
import streetlight.web.pages.configureAppFooter

fun TagScope.appFooter(
    sourcePath: String,
    vararg additional: ExtraLink
): HTMLElement {
    val element = column {
        configureAppFooter(sourcePath, *additional)
    }

    queryAndInitLotties(element)
    return element
}