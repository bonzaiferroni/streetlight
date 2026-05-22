package streetlight.web.ui

import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.html.heading3
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationEdit
import streetlight.web.EventScoutRoute

fun RenderContext.viewEventScout(galaxy: Galaxy) {
    val locationEditor = app.getLocationEditor(LocationEdit(), renderScope)
    val location = app.getLocationScout(galaxy, locationEditor, renderScope)
    val editor = app.getEventEditor(EventEdit(), renderScope)
    val model = app.getEventScout(galaxy, editor, location, renderScope)

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
        viewEventScout(galaxy)
    }
}