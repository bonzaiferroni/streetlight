package streetlight.web.ui

import koala.SvgFile
import koala.dom.RenderContext
import koala.dom.querySelectorAll
import kotlinx.browser.document
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLImageElement
import streetlight.web.model.Streetlight
import streetlight.web.pages.StarBadgeKey

fun RenderContext.wireBadges(app: Streetlight) {
    renderScope.launch {
        app.gate.starFlow.collect { star ->
            val elements = document.body?.querySelectorAll(StarBadgeKey.Class) ?: return@collect
            elements.forEach {
                val element = it as? HTMLImageElement ?: return@forEach
                element.src = star?.avatarUrl ?: SvgFile.Someone.path
            }
        }
    }
}