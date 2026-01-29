package streetlight.web

import kotlinx.browser.document
import kotlinx.coroutines.launch
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation
import streetlight.model.data.EventType

const val STOP_ZOOM = 14

fun AppContext.attachMapWindow(
    maplibre: maplibregl.Map
) {
    val eventMap = home.eventMap
    val eventMarkers = mutableMapOf<EventId, MarkerElement>()

    maplibre.on("move") {
        val bounds = maplibre.getBounds().toGeoBounds()
        val zoom = maplibre.getZoom()
        eventMap.setBounds(bounds, zoom.toFloat())
    }

    appScope.launch {
        eventMap.stateFlow.mapDistinct { it.events }.collect { events ->
            console.log("adding events")
            events.forEach { event ->
                if (eventMarkers.contains(event.eventId)) return@forEach
                console.log("adding event marker")
                val mapMarker = createEventMarker(event)
                mapMarker.marker.addTo(maplibre)
                eventMarkers[event.eventId] = mapMarker
            }
        }
    }
}

fun createEventMarker(event: EventLocation): MarkerElement {
    val element = document.createDiv()

    val iconClass = when(event.eventType) {
        EventType.Show -> "performance-icon"
        EventType.Food -> "food-icon"
        EventType.Fellowship -> "social-icon"
    }

    val icon = document.createDiv()
    icon.className = "map-marker-icon $iconClass"
    element.appendChild(icon)

    val marker = maplibregl.Marker(jsObject {
        this.element = element
        subpixelPositioning = true
    })
    marker.setLngLat(event.geoPoint.toLngLat())

    return MarkerElement(
        marker = marker,
        element = element
    )
}