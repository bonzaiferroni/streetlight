package streetlight.web.ui

import koala.dom.RenderContext
import koala.dom.ViewContext
import koala.dom.column
import koala.dom.textBlock
import koala.dom.viewGeoMap
import streetlight.model.data.Galaxy
import streetlight.web.EventScoutRoute
import streetlight.web.model.EventScout
import streetlight.web.model.Streetlight

fun RenderContext.viewEventScout(app: Streetlight, galaxy: Galaxy) {
    val model = EventScout(app, renderScope)

    column {
        viewGeoMap(app.geoMap, app.appScope)
    }
}

fun ViewContext<Streetlight>.viewEventScoutRoute() {
    routeBlock<EventScoutRoute, Galaxy>({
        api.readGalaxy(it.pathId)
    }) { galaxy ->
        viewEventScout(model, galaxy)
    }
}