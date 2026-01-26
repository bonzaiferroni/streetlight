package streetlight.web

import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.html.dom.append
import kotlinx.html.js.p
import streetlight.model.data.EventId
import streetlight.model.data.EventLocation

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
    element.append {
        p { +"event" }
    }

    val marker = maplibregl.Marker()
    marker.setLngLat(event.geoPoint.toLngLat())

    return MarkerElement(
        marker = marker,
        element = element
    )
}