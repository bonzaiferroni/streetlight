package streetlight.web.ui

import kampfire.model.thumb
import koala.SvgFile
import koala.dom.RenderContext
import koala.dom.querySelector
import kotlinx.browser.document
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLImageElement
import streetlight.web.model.Streetlight
import streetlight.web.pages.StarBadgeKey

fun RenderContext.wireBadge(app: Streetlight) {
    val element = document.body?.querySelector(StarBadgeKey.Id) as? HTMLImageElement ?: error("star badge not found")

    renderScope.launch {
        app.gate.starFlow.collect { star ->
            element.src = (star?.images.thumb ?: SvgFile.Someone.url).value
        }
    }
}