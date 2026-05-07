package streetlight.web.shells

import kampfire.model.small
import koala.css.*
import koala.html.btn
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyLight
import streetlight.web.GalaxyRoute

fun FlowContent.buttonOf(star: GalaxyLight, modifiers: ModifierSet? = null) {
    btn(star.name, GalaxyRoute(star.slug), star.imageUrl, modifiers)
}

fun FlowContent.buttonOf(galaxy: Galaxy, modifiers: ModifierSet? = null) {
    btn(
        text = galaxy.name,
        route = GalaxyRoute(galaxy.slug),
        background = galaxy.images.small,
        modifiers = modify(modifiers)
    )
}