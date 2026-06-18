package streetlight.web.ui

import kampfire.model.thumb
import koala.SiteImage
import koala.SvgFile
import koala.dom.AppScope
import koala.dom.querySelector
import kotlinx.browser.document
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLImageElement
import streetlight.web.model.StarSession
import streetlight.web.pages.StarBadgeKey

fun AppScope.wireBadge() {
    val gate = app.get<StarSession>()

    val element = document.body?.querySelector(StarBadgeKey.Id) as? HTMLImageElement ?: error("star badge not found")

    launchEffect {
        gate.starFlow.collect { star ->
            val url = when (star) {
                null -> SvgFile.Someone.url
                else -> star.images.thumb ?: SiteImage.placeholderTh.url
            }
            element.src = url.value
        }
    }
}