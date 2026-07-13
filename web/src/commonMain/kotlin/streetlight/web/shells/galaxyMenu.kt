package streetlight.web.shells

import koala.css.FlexWrap
import koala.css.modify
import koala.html.Id
import koala.html.ButtonPopover
import koala.html.btn
import koala.html.buttonPopover
import koala.html.card
import koala.html.row
import koala.html.setJsonData
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.ui.HomeRoute
import streetlight.web.layouts.GalaxyKey

fun FlowContent.galaxyMenu(
    galaxies: List<Galaxy>,
    currentGalaxy: Galaxy?,
) {
    buttonPopover("galaxies", id = GalaxyMenuKey.Id) {
        card(modify(ButtonPopover.CardMod)) {
            setJsonData(GalaxyKey.TopGalaxies, galaxies)

            row(GalaxyMenuKey.RowMods) {
                galaxyMenuItems(galaxies, currentGalaxy)
            }
        }
    }
}

fun FlowContent.galaxyMenuItems(
    galaxies: List<Galaxy>,
    currentGalaxy: Galaxy?,
) {
    if (currentGalaxy != null) {
        btn("Home", HomeRoute)
    }
    galaxies.forEach {
        if (it.name == currentGalaxy?.name) return@forEach
        buttonOf(it)
    }
}

object GalaxyMenuKey {
    val Id = Id("galaxy-menu")
    // val Anchor = Anchor("galaxy-menu-anchor")
    val RowMods = modify(FlexWrap)
}