package streetlight.web.ui

import koala.core.findAndInitLotties
import koala.dom.RenderContext
import koala.dom.row
import org.w3c.dom.HTMLElement
import streetlight.web.pages.configureAppFooter

fun RenderContext.appFooter(): HTMLElement {
    val element = row {
        configureAppFooter()
    }
    findAndInitLotties(element)
    return element
}