package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.html.heading3
import koala.html.section
import koala.model.mapDistinct
import streetlight.model.data.Galaxy
import streetlight.web.LocationScoutRoute
import streetlight.web.model.LocationFinder
import streetlight.web.model.Streetlight

fun RenderContext.viewLocationScout(app: Streetlight, galaxy: Galaxy) {
    val sectionMod = modify()
    val finder = LocationFinder(renderScope, app)
    val locationFlow = finder.stateFlow.mapDistinct { it.location }

    column(modify(Gap8)) {
        section(sectionMod) {
            filigree {
                heading1("Location Scout", modify(Shrinkable))
            }
            heading3("Posting to ${galaxy.name}", modify(OpacityMost, TextAlignCenter))
        }

        section(sectionMod) {
            filigree {
                heading3("Find a location")
            }
            viewContextOf(finder) {
                viewLocationFinder()
            }
        }

        flowBlock(locationFlow, defaultMagic) { location ->
            val location = location ?: return@flowBlock
            textBlock("Do something with location: ${location.name}")
        }

        appFooter("")
    }
}

fun ViewContext<Streetlight>.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.pathId)
    }) { galaxy ->

        viewLocationScout(model, galaxy)
    }
}