package streetlight.web.shells

import koala.css.*
import koala.html.button
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyStar
import streetlight.web.GalaxyPathIdRoute

fun FlowContent.buttonOf(star: GalaxyStar, modifiers: ModifierSet? = null) {
    button(star.name, GalaxyPathIdRoute(star.pathId), star.imageUrl, modifiers)
}