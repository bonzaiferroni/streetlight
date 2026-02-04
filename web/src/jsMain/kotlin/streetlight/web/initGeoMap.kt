package streetlight.web

import koala.css.Css
import koala.dom.*
import kotlinx.browser.document
import kotlinx.coroutines.launch

fun RenderContext.initGeoMap(app: AppContext) {
    val geoMap = app.home.geoMap
    val element = document.getElementById(GeoMapIds.widget.value)
    console.log(element)
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

        val markers = mutableMapOf<MapEntityId, MarkerObject>()

        val context = object: MapContext {
            override val markers = markers
            override val widget = widget
        }

        launch {
            geoMap.entityFlow.collect { entity ->
                context.collectEntity(entity)
            }
        }

        launch {
            geoMap.removeEntity.collect { entityId ->
                markers[entityId]?.marker?.remove()
                markers.remove(entityId)
            }
        }

        launch {
            launch {
                geoMap.zoomFlow.collect { zoom ->
                    markers.forEach { (_, obj) ->
                        val minZoom = obj.entity.minZoom ?: return@forEach
                        obj.setOpacity(if (zoom >= minZoom) 1f else 0f)
                    }
                }
            }
        }
    }
}

interface MapContext {
    val markers: MutableMap<MapEntityId, MarkerObject>
    val widget: maplibregl.Map
}

fun MapContext.collectEntity(entity: MapEntity) {
    when (entity) {
        is MarkerEntity -> {
            val markerElement = recallMarker(entity) ?: addMarker(entity)
            markerElement.setAttributes(entity)
        }
    }
}

fun MapContext.recallMarker(entity: MarkerEntity): MarkerObject? {
    val markerElement = markers[entity.entityId] ?: return null
    val current = markerElement.marker.getLngLat()
    val destination = entity.position.toLngLat()
    val distance = current.distanceTo(destination)
    if (distance > 1) {
        markerElement.marker.move(current, destination)
    } else {
        markerElement.marker.setLngLat(destination)
    }
    return markerElement
}

private fun MapContext.addMarker(entity: MarkerEntity): MarkerObject {
    val element = entity.iconPath?.let {
        document.createDiv().also {
            it.modify(MarkerClass.base)
        }
    }

    val bearingElement = entity.bearing?.let { _ ->
        if (element != null) {
            document.createDiv().also {
                it.modify(MarkerClass.bearing)
                element.appendChild(it)
            }
        } else null
    }

    val iconElement = entity.iconPath?.let { iconPath ->
        if (element != null) {
            document.createDiv().also {
                it.style.setProperty("--svg", "url(${iconPath})")
                it.modify(MarkerClass.icon)
                element.appendChild(it)
            }
        } else null
    }

    val options = jsObject {
        this.element = element
        subpixelPositioning = entity.subpixelPositioning
    }
    val markerObject = MarkerObject(
        marker = maplibregl.Marker(
            options = options
        ),
        entity = entity,
        element = element,
        iconElement = iconElement,
        bearingElement = bearingElement
    )
    val onClick = entity.onClick
    if (element != null && onClick != null) {
        element.addEventListener("click", callback = {
            onClick()
        })
    }
    markerObject.marker.setLngLat(entity.position.toLngLat())
    markerObject.marker.addTo(widget)
    markers[entity.entityId] = markerObject
    return markerObject
}

private fun MarkerObject.setAttributes(entity: MarkerEntity) {
    entity.bearing?.let {
        setBearing(it)
    }
    entity.opacity?.let {
        setOpacity(it)
    }
}

object MarkerClass {
    val base = Css("map-marker")
    val bearing = Css("marker-bearing")
    val icon = Css("marker-icon")
}