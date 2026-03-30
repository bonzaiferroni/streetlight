package streetlight.web.ui

import koala.core.initLottie
import koala.core.queryAndInitLotties
import koala.dom.DOMContext
import koala.dom.column
import org.w3c.dom.HTMLElement
import streetlight.web.pages.configureAppFooter

fun DOMContext.appFooter(sourcePath: String): HTMLElement {
    val element = column {
        configureAppFooter(sourcePath)
    }

    queryAndInitLotties(element)
    return element
}