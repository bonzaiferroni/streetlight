package streetlight.web.ui

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
import streetlight.web.EarthRoute
import streetlight.web.model.DataCache
import streetlight.web.shells.GalaxyMenuKey

fun RenderContext.galaxyEarthMenu(
    currentGalaxy: Galaxy?,
    modifiers: ModifierSet? = null
) {
    val cache = app.get<DataCache>()
    val galaxies = cache.galaxyLights.stateNow.items

    box(modifiers) {
        buttonPopover(currentGalaxy?.name ?: "Galaxies") {
            card(modify(ButtonPopover.CardMod)) {
                row(GalaxyMenuKey.RowMods) {
                    if (currentGalaxy != null) {
                        btn("Top", EarthRoute(null))
                    }
                    galaxies.forEach { galaxy ->
                        if (galaxy.name == currentGalaxy?.name) return@forEach
                        btn(
                            text = galaxy.name,
                            route = EarthRoute(galaxy.slug),
                            background = galaxy.images.small,
                            modifiers = modify(modifiers)
                        )
                    }
                }
            }
        }
    }
}