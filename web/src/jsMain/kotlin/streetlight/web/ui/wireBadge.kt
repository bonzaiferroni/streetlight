package streetlight.web.ui

import koala.SiteImage
import koala.SvgFile
import koala.dom.ViewScope
import koala.dom.querySelector
import streetlight.web.model.SessionClient
import streetlight.web.pages.StarBadgeKey
import web.dom.document
import web.html.HTMLImageElement

/** Shows the signed-in star's image in the star badge. */
fun ViewScope.wireBadge() {
    val gate = app.get<SessionClient>()

    val element = document.body.querySelector(StarBadgeKey.Id) as? HTMLImageElement ?: error("star badge not found")

    launchEffect {
        gate.starState.flow.collect { star ->
            val url = when (star) {
                null -> SvgFile.Someone.url
                else -> star.image?.thumb ?: SiteImage.placeholderTh
            }
            element.src = url.value
        }
    }
}