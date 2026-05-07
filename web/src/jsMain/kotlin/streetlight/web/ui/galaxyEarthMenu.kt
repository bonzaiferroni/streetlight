package streetlight.web.ui

import kampfire.model.medium
import kampfire.model.small
import koala.css.ModifierSet
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.box
import koala.html.ButtonPopover
import koala.html.btn
import koala.html.buttonPopover
import koala.html.card
import koala.html.row
import streetlight.model.data.Galaxy
import streetlight.web.EarthMapRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyMenuKey
import streetlight.web.shells.buttonOf

fun RenderContext.galaxyEarthMenu(
    currentGalaxy: Galaxy?,
    app: Streetlight,
    modifiers: ModifierSet? = null
) {
    val galaxies = app.cache.galaxyLights.stateNow.items

    box(modifiers) {
        buttonPopover(currentGalaxy?.name ?: "Galaxies") {
            card(modify(ButtonPopover.CardMod)) {
                row(GalaxyMenuKey.RowMods) {
                    if (currentGalaxy != null) {
                        btn("Top", EarthMapRoute(null))
                    }
                    galaxies.forEach { galaxy ->
                        if (galaxy.name == currentGalaxy?.name) return@forEach
                        btn(
                            text = galaxy.name,
                            route = EarthMapRoute(galaxy.slug),
                            background = galaxy.images.small,
                            modifiers = modify(modifiers)
                        )
                    }
                }
            }
        }
    }
}