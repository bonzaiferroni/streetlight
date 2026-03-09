package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.headerOf
import koala.model.mapDistinct
import streetlight.web.model.Streetlight
import streetlight.web.model.GalaxyFoundry

fun RenderContext.viewGalaxyFoundry(app: Streetlight) {
    val model = GalaxyFoundry(app, renderScope)
    val geoMap = app.geoMap
    val nameFlow = model.galaxyFlow.mapDistinct { it.name ?: "" }
    val blobFlow = model.stateFlow.mapDistinct { it.blobUrl }

    column {
        viewGeoMap(geoMap, app.appScope)
        row {
            textField("name", modify(Flex1), model::setName, nameFlow)
            button("Found Galaxy", onClick = model::foundGalaxy)
        }
        imageDrop(blobFlow, model::setBlobUrl) {
            box {
                headerOf(model.stateNow.galaxy.name ?: "", it)
            }
        }
    }
}