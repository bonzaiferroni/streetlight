package streetlight.web.ui

import koala.core.initLottie
import koala.core.queryAndInitLotties
import koala.dom.DOMContext
import koala.dom.column
import org.w3c.dom.HTMLElement
import streetlight.model.data.ExtraLink
import streetlight.web.pages.configureAppFooter

fun DOMContext.appFooter(
    sourcePath: String,
    vararg additional: ExtraLink
): HTMLElement {
    val element = column {
        configureAppFooter(sourcePath, *additional)
    }

    queryAndInitLotties(element)
    return element
}