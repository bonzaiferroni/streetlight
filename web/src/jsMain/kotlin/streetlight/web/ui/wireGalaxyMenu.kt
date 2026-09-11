package streetlight.web.ui

import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.web.layouts.GalaxyKey
import streetlight.web.shells.GalaxyMenuKey
import streetlight.web.shells.galaxyMenuItems
import web.html.HTMLElement

//fun ViewScope.wireGalaxyMenu(
//    root: HTMLElement,
//    currentGalaxy: Galaxy?
//) {
//    val cache = app.get<DataCache>()
//
//    val result = root.queryAttribute(GalaxyKey.TopGalaxies) ?: return
//    val topGalaxies = result.value
//    val element = result.element
//
//    contentScope.launch {
//        cache.galaxyStars.itemsFlow.collect { galaxies ->
//            val galaxies = galaxies.takeIf { it.isNotEmpty() } ?: topGalaxies
//            element.clear()
//            element.append {
//                row(GalaxyMenuKey.RowMods) {
//                    galaxyMenuItems(galaxies, currentGalaxy)
//                }
//            }
//        }
//    }
//}