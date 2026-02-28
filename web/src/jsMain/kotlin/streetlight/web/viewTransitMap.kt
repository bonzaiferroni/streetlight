package streetlight.web

import koala.dom.RenderContext
import koala.external.maplibregl
import koala.model.PointEntityView
import koala.model.jsObject
import koala.model.mapDistinct
import koala.model.mapDistinctBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import streetlight.model.data.TransitRoute
import streetlight.model.data.VehicleType
import kotlin.js.json

const val STOP_ZOOM = 14

fun RenderContext.viewTransitMap(
    app: AppContext,
    maplibre: maplibregl.Map
) {
    val streetMap = app.streetMap
    val transitMap = app.streetMap.transit
    var stopMarkers: List<PointEntityView> = emptyList()
    var areaTransit = transitMap.stateNow.areaTransit
    val vehicleElements = mutableMapOf<String, PointEntityView>()
    var markersVisible = false

    renderScope.launch {
        launch {
            streetMap.stateFlow.mapDistinct { it.zoom }.collect { zoom ->
                if (!markersVisible && zoom >= STOP_ZOOM) {
                    console.log("showing markers")
                    markersVisible = true
                    stopMarkers.forEach { marker ->
                        marker.setOpacity(1f)
                    }
                } else if (markersVisible && zoom < STOP_ZOOM) {
                    console.log("hiding markers")
                    markersVisible = false
                    stopMarkers.forEach { marker ->
                        marker.setOpacity(0f)
                    }
                }
            }
        }

        launch {
            transitMap.stateFlow.mapDistinctBy({ it.communityId }) { it.areaTransit }.filterNotNull().collect { transit ->
                areaTransit = transit
                // stopMarkers = transit.stops.map { createStopMarker(it, eventMap.stateNow.zoom, maplibre) }
                addRouteLines(transit.routes, maplibre)
            }
        }

        launch {
//            transitMap.stateFlow.mapDistinctBy({ it.timestamp }) { it.vehiclePositions }.collect { vehicles ->
//                val currentTime = Date.now().toLong() / 1000
//                vehicles.forEach { vehicle ->
//                    val position = vehicle.position ?: return@forEach
//                    val vehicleId = vehicle.vehicle?.id ?: return@forEach
//                    val route = areaTransit?.routes?.firstOrNull() { it.transitRouteId.value == vehicle.trip?.routeId }
//                        ?: return@forEach
//
//                    val bus = if (vehicleElements.containsKey(vehicleId)) {
//                        val bus = vehicleElements.getValue(vehicleId)
//                        val current = bus.marker.getLngLat()
//                        val destination = position.toLngLat()
//                        val distance = current.distanceTo(destination)
//                        if (distance > 1) {
//                            bus.marker.move(current, destination)
//                        } else {
//                            bus.marker.setLngLat(destination)
//                        }
//                        bus.setBearing(position.bearing ?: 0f)
//                        bus
//                    } else {
//                        val bus = createBusMarker(position, route)
//                        bus.marker.setLngLat(position.toLngLat())
//                            .addTo(maplibre)
//                        vehicleElements[vehicleId] = bus
//                        bus
//                    }
//
//                    vehicle.timestamp?.let {
//                        val vehicleTime = vehicle.timestamp.toString().toLong()
//                        val secondsSinceCapture = (currentTime - vehicleTime).toInt()
//                        val opacity = (1 - secondsSinceCapture / 240f).coerceIn(.5f, 1f)
//                        bus.marker.setOpacity(opacity.toString())
//                    }
//                }
//
//                val missingIds = vehicleElements.map { it.key }
//                    .filter { vehicleId -> vehicles.none { it.vehicle?.id == vehicleId } }
//                missingIds.forEach {
//                    vehicleElements[it]?.marker?.remove()
//                    vehicleElements.remove(it)
//                }
        }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
fun addRouteLines(
    routes: List<TransitRoute>,
    maplibre: maplibregl.Map,
) {
    val lines = routes.mapNotNull { route ->
        if (route.vehicleType == VehicleType.Bus) return@mapNotNull null
        val line = route.points.map { arrayOf(it.lng, it.lat) }.toJsArray()
        jsObject {
            type = "Feature"
            properties = koala.model.jsObject { }
            geometry = koala.model.jsObject {
                type = "LineString"
                coordinates = line
            }
        }
    }.toJsArray()
    val sourceObj = jsObject {
        type = "geojson"
        data = koala.model.jsObject {
            type = "FeatureCollection"
            features = lines
        }
    }

    val layerObj = jsObject {
        id = "routes"
        type = "line"
        source = "routes"
        paint = json(
            "line-color" to "#4fd1c5",
            "line-width" to 2
        )
        layout = json(
            "line-join" to "round",
            "line-cap" to "round"
        )
    }

     maplibre.addSource("routes", sourceObj);
     maplibre.addLayer(layerObj)
}

//fun createBusMarker(position: Position, route: TransitRoute): MarkerObject {
//    val element = document.createDiv()
//    element.className = "map-marker"
//
//    element.addEventListener("click", callback = {
//        console.log(route.longName + " selected")
//    })
//
//    val bearingElement = document.createDiv()
//    bearingElement.className = "bus-bearing"
//    element.appendChild(bearingElement)
//
//    val iconClass = if (route.vehicleType == VehicleType.LightRail) "train-icon" else "bus-icon"
//    val icon = document.createDiv()
//    icon.className = "map-marker-icon $iconClass"
//    element.appendChild(icon)
//
//    val options = jsObject {
//        this.element = element
//        subpixelPositioning = true
//    }
//
//    val markerObject = MarkerObject(
//        marker = maplibregl.Marker(
//            options = options
//        ),
//        element = element,
//        bearingElement = bearingElement
//    )
//    markerObject.setBearing(position.bearing ?: 0f)
//    return markerObject
//}

//fun createStopMarker(stop: TransitStop, zoom: Float, map: maplibregl.Map): MarkerObject {
//    val element = document.createDiv()
//    element.className = "map-marker"
//
//    val icon = document.createDiv()
//    icon.className = "map-marker-icon transit-stop"
//    element.appendChild(icon)
//
//    val marker = maplibregl.Marker(
//        options = jsObject {
//            this.element = element
//        }
//    )
//    marker.setLngLat(maplibregl.LngLat(stop.longitude, stop.latitude))
//    marker.addTo(map)
//
////    // road blocked: can't get marker events to work
////    marker.setEventedParent(map)
////    console.log(marker.listens("zoomend"))
////    marker.on("zoomend") {
////        println("ey: $it")
////        val show = map.getZoom() >= 14
////        marker.getElement().style.display = if (show) "block" else "none"
////    }
//
//    val markerObject = MarkerObject(marker, element, null)
//    markerObject.setOpacity(if (zoom >= STOP_ZOOM) 1f else 0f)
//    return markerObject
//}