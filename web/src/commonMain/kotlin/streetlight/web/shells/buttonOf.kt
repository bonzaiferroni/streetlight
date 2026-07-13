package streetlight.web.shells

import koala.css.*
import koala.html.btn
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.ui.GalaxyRoute

fun FlowContent.buttonOf(galaxy: Galaxy, mod: ModifierSet? = null) {
    btn(
        text = galaxy.name,
        route = GalaxyRoute(galaxy.slug),
        background = galaxy.image?.small,
        modifiers = modify(mod)
    )
}