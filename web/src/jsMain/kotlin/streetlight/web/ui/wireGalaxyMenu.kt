package streetlight.web.ui

import koala.dom.*
import koala.utils.prettyPrint
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import streetlight.model.data.Galaxy
import streetlight.web.layouts.GalaxyKey
import streetlight.web.model.Streetlight
import streetlight.web.shells.GalaxyMenuKey
import streetlight.web.shells.galaxyMenuItems

fun RenderContext.wireGalaxyMenu(
    app: Streetlight,
    root: HTMLElement,
    currentGalaxy: Galaxy?
) {
    val result = root.queryJsonAttribute(GalaxyKey.TopGalaxies) ?: return
    val topGalaxies = result.value
    val element = result.element

    renderScope.launch {
        app.cache.galaxy.galaxiesFlow.collect { galaxies ->
            val galaxies = galaxies.takeIf { it.isNotEmpty() } ?: topGalaxies
            element.clear()
            element.append {
                row(GalaxyMenuKey.RowMods) {
                    galaxyMenuItems(galaxies, currentGalaxy)
                }
            }
        }
    }
}