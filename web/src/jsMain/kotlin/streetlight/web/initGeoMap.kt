package streetlight.web

import koala.dom.*
import kotlinx.browser.document
import kotlinx.coroutines.launch

fun RenderContext.initGeoMap(app: AppContext) {
    val geoMap = app.home.geoMap
    val element = document.getElementById(GeoMapIds.widget.value)
    val widget = maplibregl.Map(jsObject {
        container = element
        style = "https://tiles.openfreemap.org/styles/fiord"
        center = maplibregl.LngLat(-104.95, 39.75)
        zoom = 11
    })

    widget.addControl(maplibregl.NavigationControl())
    widget.addControl(maplibregl.FullscreenControl())

    renderScope.launch {
        widget.on("move") {
            val bounds = widget.getBounds().toGeoBounds()
            val zoom = widget.getZoom()
            geoMap.setBounds(bounds, zoom.toFloat())
        }

        val markers = mutableMapOf<MapEntityId, MapObject>()

        val context = MapContext(widget)

        launch {
            geoMap.entityFlow.collect(context::collectEntity)
        }

        launch {
            geoMap.removeEntity.collect { entityId ->
                markers[entityId]?.marker?.remove()
                markers.remove(entityId)
            }
        }

        launch {
            geoMap.zoomFlow.collect { zoom ->
                markers.forEach { (_, obj) ->
                    val minZoom = obj.entity.minZoom ?: return@forEach
                    obj.setOpacity(if (zoom >= minZoom) 1f else 0f)
                }
            }
        }

        launch {
            geoMap.linesFlow.collect(context::showLines)
        }
    }
}