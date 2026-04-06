package streetlight.web.shells

import koala.css.*
import koala.html.btn
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyLight
import streetlight.web.GalaxySlugRoute

fun FlowContent.buttonOf(star: GalaxyLight, modifiers: ModifierSet? = null) {
    btn(star.name, GalaxySlugRoute(star.path), star.imageUrl, modifiers)
}

fun FlowContent.buttonOf(galaxy: Galaxy, modifiers: ModifierSet? = null) {
    btn(
        text = galaxy.name,
        route = GalaxySlugRoute(galaxy.slug),
        background = galaxy.imageUrl,
        modifiers = modify(modifiers)
    )
}