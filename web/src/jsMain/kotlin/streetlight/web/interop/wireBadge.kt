package streetlight.web.interop

import koala.SiteImage
import koala.modifier.Css
import koala.modifier.DisplayNone
import koala.modifier.removeStyle
import koala.modifier.setStyle
import koala.dom.ViewScope
import koala.dom.querySelector
import streetlight.web.model.SessionClient
import streetlight.web.component.StarBadgeKey
import web.dom.document
import web.html.HTMLImageElement
import streetlight.web.utility.element

/** Shows the signed-in star's image in the star badge, and its icon while signed out. */
fun ViewScope.wireBadge() {
    val gate = app.get<SessionClient>()

    val element = document.body.querySelector(StarBadgeKey.Id) as? HTMLImageElement ?: error("star badge not found")
    val icon = document.body.querySelector(StarBadgeKey.IconId) ?: error("star badge icon not found")

    launchEffect {
        gate.starState.flow.collect { star ->
            if (star == null) {
                element.setStyle(DisplayNone)
                icon.removeStyle(Css.Display)
                return@collect
            }
            element.src = (star.image?.thumb ?: SiteImage.placeholderTh).value
            element.removeStyle(Css.Display)
            icon.setStyle(DisplayNone)
        }
    }
}