package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.html.heading3
import streetlight.model.data.Galaxy
import streetlight.web.EventScoutRoute
import streetlight.web.io.handleResponse
import streetlight.web.model.EventScout
import streetlight.web.model.Streetlight
import streetlight.web.ui.viewLocationFinder

fun RenderContext.viewEventScout(model: EventScout, galaxy: Galaxy) {

    section {
        column(modify(Gap0)) {
            heading1("Event Scout", modify(LineHeight115))
            filigree {
                heading3("Posting to ${galaxy.name}", modify(LineHeight115, OpacityMost))
            }
        }

        card(modify(Padding0)) {
            geoMapMount(geoMap, appScope, modifiers = modify(Height48))

//            viewContextOf(model.location) {
//                viewLocationFinder()
//            }
        }
    }
}

fun RenderContext.viewEventScoutRoute() {
    routeBlock<EventScoutRoute, Galaxy>({
        api.readGalaxy(it.slug).handleResponse(toaster::toast)
    }) { galaxy ->
        val model = EventScout(galaxy, renderScope)
        viewEventScout(model, galaxy)
    }
}