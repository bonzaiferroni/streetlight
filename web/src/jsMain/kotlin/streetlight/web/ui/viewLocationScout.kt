package streetlight.web.ui

import koala.dom.ViewContext
import koala.dom.textBlock
import streetlight.model.data.Galaxy
import streetlight.web.EventScoutRoute
import streetlight.web.LocationScoutRoute
import streetlight.web.model.Streetlight

fun ViewContext<Streetlight>.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.pathId)
    }) { galaxy ->
        textBlock(galaxy.name)
        // viewLocationScout(model, galaxy)
    }
}