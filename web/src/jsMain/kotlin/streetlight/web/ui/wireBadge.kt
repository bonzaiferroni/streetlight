package streetlight.web.ui

import koala.SiteImage
import koala.SvgFile
import koala.dom.ViewScope
import koala.dom.querySelector
import kotlinx.browser.document
import org.w3c.dom.HTMLImageElement
import streetlight.web.model.StarSession
import streetlight.web.pages.StarBadgeKey

fun ViewScope.wireBadge() {
    val gate = app.get<StarSession>()

    val element = document.body?.querySelector(StarBadgeKey.Id) as? HTMLImageElement ?: error("star badge not found")

    launchEffect {
        gate.starField.flow.collect { star ->
            val url = when (star) {
                null -> SvgFile.Someone.url
                else -> star.image?.thumb ?: SiteImage.placeholderTh
            }
            element.src = url.value
        }
    }
}