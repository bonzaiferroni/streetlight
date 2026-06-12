package streetlight.web.ui

import kampfire.model.thumb
import koala.SvgFile
import koala.dom.AppScope
import koala.dom.querySelector
import kotlinx.browser.document
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLImageElement
import streetlight.web.model.UserGate
import streetlight.web.pages.StarBadgeKey

fun AppScope.wireBadge() {
    val gate = app.get<UserGate>()

    val element = document.body?.querySelector(StarBadgeKey.Id) as? HTMLImageElement ?: error("star badge not found")

    parentScope.launch {
        gate.starFlow.collect { star ->
            element.src = (star?.images.thumb ?: SvgFile.Someone.url).value
        }
    }
}