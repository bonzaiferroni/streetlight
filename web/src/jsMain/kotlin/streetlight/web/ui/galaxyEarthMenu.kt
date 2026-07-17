package streetlight.web.ui

import koala.css.ModifierSet
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.box
import koala.html.ButtonPopover
import koala.html.btn
import koala.html.buttonPopover
import koala.html.card
import koala.html.row
import streetlight.model.data.Galaxy
import streetlight.model.ui.GalaxyMapRoute
import streetlight.web.model.DataCache
import streetlight.web.shells.GalaxyMenuKey

fun ViewScope.galaxyEarthMenu(
    currentGalaxy: Galaxy?,
    mod: ModifierSet? = null
) {
    val cache = app.get<DataCache>()
    val galaxies = cache.galaxyLights.stateNow.items

    box(mod) {
        buttonPopover(currentGalaxy?.name ?: "Galaxies") {
            card(modify(ButtonPopover.CardMod)) {
                row(GalaxyMenuKey.RowMods) {
                    if (currentGalaxy != null) {
                        btn("Top", GalaxyMapRoute(null))
                    }
                    galaxies.forEach { galaxy ->
                        if (galaxy.name == currentGalaxy?.name) return@forEach
                        btn(
                            text = galaxy.name,
                            route = GalaxyMapRoute(galaxy.slug),
                            background = galaxy.image?.small,
                            modifiers = modify(mod)
                        )
                    }
                }
            }
        }
    }
}