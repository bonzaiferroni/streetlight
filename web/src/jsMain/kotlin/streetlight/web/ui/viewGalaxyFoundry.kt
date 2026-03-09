package streetlight.web.ui

import koala.css.*
import koala.dom.*
import kotlinx.coroutines.launch
import streetlight.model.data.GalaxyEdit
import streetlight.web.model.AppContext

fun RenderContext.viewGalaxyFoundry(app: AppContext) {
    val geoMap = app.geoMap
    var name = ""

    column {
        viewGeoMap(geoMap, app.appScope)
        row {
            textField("name", modify(Flex1), { name = it })
            button("Found Galaxy", onClick = {
                renderScope.launch {
                    app.client.api.foundGalaxy(GalaxyEdit(
                        name = name,
                        center = geoMap.stateNow.center
                    ))
                }
            })
        }
    }
}