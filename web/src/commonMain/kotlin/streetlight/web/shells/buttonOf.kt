package streetlight.web.shells

import koala.css.*
import koala.html.btn
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyStar
import streetlight.web.GalaxyPathIdRoute

fun FlowContent.buttonOf(star: GalaxyStar, modifiers: ModifierSet? = null) {
    btn(star.name, GalaxyPathIdRoute(star.pathId), star.imageUrl, modifiers)
}

fun FlowContent.buttonOf(galaxy: Galaxy, modifiers: ModifierSet? = null) {
    btn(
        text = galaxy.name,
        route = GalaxyPathIdRoute(galaxy.pathId),
        background = galaxy.imageUrl,
        modifiers = modify(modifiers)
    )
}