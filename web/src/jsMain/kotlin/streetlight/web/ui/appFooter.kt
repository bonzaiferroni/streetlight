package streetlight.web.ui

import koala.core.findAndInitLotties
import koala.core.initLottie
import koala.css.AlignItemsCenter
import koala.css.Gap0
import koala.css.Italic
import koala.css.JustifyCenter
import koala.css.Opacity6
import koala.css.Width100
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.lottie
import koala.dom.row
import koala.html.column
import koala.html.textBlock
import kotlinx.html.style
import org.w3c.dom.HTMLElement
import streetlight.web.pages.configureAppFooter

fun RenderContext.appFooter(): HTMLElement {
    val element = row {
        configureAppFooter()
    }
    findAndInitLotties(element)
    return element
}