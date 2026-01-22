@file:Suppress("UnsafeCastFromDynamic")
@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web

import kabinet.model.GeoPoint
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.TransitRoute
import streetlight.model.data.TransitStop
import streetlight.model.data.VehicleType
import kotlin.js.Date
import kotlin.js.json
import kotlin.time.Duration.Companion.seconds

external var geoMap: maplibregl.Map
private val vehicleElements = mutableMapOf<String, MarkerElement>()
private val stopElements = mutableListOf<MarkerElement>()
private var timestamp = 0L
private val gtfsClient = GtfsBrowserClient()
private var mapZoom = geoMap.getZoom()
const val STOP_ZOOM = 14
val mainScope = MainScope()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun initMap() {
    val app = BrowserProvider()
    val stage = MapStage(mainScope, app)
    initMapData(stage)
    viewMapStage(stage)
}

fun initMapData(stage: MapStage) {
    mainScope.launch {
        val feedType = loadFeedType()
        val streetTransit = gtfsClient.readRoutes()
        createStops(streetTransit.stops)

        addZoomReactions()
        addRouteLines(streetTransit.routes)

        geoMap.on("move") {
            val point = geoMap.getCenter().let { GeoPoint(it.lng, it.lat) }
            stage.setLocation(point)
        }

        while (true) {
            val vehicles = fetchVehicles(feedType, setOf("15L", "15", "121", "121L", "107R", "101H", "A"))
            val currentTime = Date.now().toLong() / 1000

            vehicles?.forEach { vehicle ->
                val position = vehicle.position ?: return@forEach
                val vehicleId = vehicle.vehicle?.id ?: return@forEach
                val route = streetTransit.routes.firstOrNull() { it.transitRouteId.value == vehicle.trip?.routeId }
                    ?: return@forEach

                val bus = if (vehicleElements.containsKey(vehicleId)) {
                    val bus = vehicleElements.getValue(vehicleId)
                    val current = bus.marker.getLngLat()
                    val destination = position.toLngLat()
                    val distance = current.distanceTo(destination)
                    if (distance > 1) {
                        bus.marker.move(current, destination)
                    } else {
                        bus.marker.setLngLat(position.toLngLat())
                    }
                    bus.setBearing(position.bearing ?: 0f)
                    bus
                } else {
                    val bus = createBus(position, route)
                    bus.marker.setLngLat(position.toLngLat())
                        .addTo(geoMap)
                    vehicleElements[vehicleId] = bus
                    bus
                }

                vehicle.timestamp?.let {
                    val vehicleTime = vehicle.timestamp.toString().toLong()
                    val secondsSinceCapture = (currentTime - vehicleTime).toInt()
                    val opacity = (1 - secondsSinceCapture / 240f).coerceIn(.5f, 1f)
                    bus.marker.setOpacity(opacity.toString())
                }
            }

            vehicles?.let {
                val missingIds = vehicleElements.map { it.key }
                    .filter { vehicleId -> vehicles.none { it.vehicle?.id == vehicleId } }
                missingIds.forEach {
                    vehicleElements[it]?.marker?.remove()
                    vehicleElements.remove(it)
                }
            }

            delay(30.seconds)
        }
    }
}

fun addZoomReactions() {
    geoMap.on("zoomend") {
        val zoom = geoMap.getZoom()
        console.log(zoom)
        if (zoom >= STOP_ZOOM) {
            console.log("showing markers")
            stopElements.forEach { me -> // markerElement
                me.setOpacity(1f)
            }
        } else {

            console.log("hiding markers")
            stopElements.forEach { me -> // markerElement
                me.setOpacity(0f)
            }
        }
        mapZoom = zoom
    }
}

fun addRouteLines(routes: List<TransitRoute>) {
    val lines = routes.mapNotNull { route ->
        if (route.vehicleType == VehicleType.Bus) return@mapNotNull null
        val line = route.points.map { arrayOf(it.lon, it.lat) }.toJsArray()
        jsObject {
            type = "Feature"
            properties = jsObject { }
            geometry = jsObject {
                type = "LineString"
                coordinates = line
            }
        }
    }.toJsArray()
    val sourceObj = jsObject {
        type = "geojson"
        data = jsObject {
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

    geoMap.addSource("routes", sourceObj);
    geoMap.addLayer(layerObj)
}

suspend fun loadFeedType(): ProtobufType {
    val root = protobuf.load("/static/proto/gtfs-realtime.proto").await()
    return root.lookupType("transit_realtime.FeedMessage")
}

suspend fun fetchVehicles(feedType: ProtobufType, routeIds: Set<String>): List<VehiclePosition>? {
    val feed = gtfsClient.readVehiclePositions(feedType)
    val latest = feed.header.timestamp.toString().toLong()
    val delta = (latest - timestamp).toInt()
    console.log("fetching vehicles -- timestamp delta: $delta")
    timestamp = latest
    if (delta == 0) return null
    return feed.entity
        .mapNotNull { it.vehicle }
        .filter { it.trip != null && routeIds.contains(it.trip.routeId) }
        .toList()
}

fun createBus(position: Position, route: TransitRoute): MarkerElement {
    val element = document.createDiv()
    element.className = "map-marker"

    element.addEventListener("click", callback = {
        console.log(route.longName + " selected")
    })

    val bearingElement = document.createDiv()
    bearingElement.className = "bus-bearing"
    element.appendChild(bearingElement)

    val iconClass = if (route.vehicleType == VehicleType.LightRail) "train-icon" else "bus-icon"
    val icon = document.createDiv()
    icon.className = "map-marker-icon $iconClass"
    element.appendChild(icon)

    val options = jsObject {
        this.element = element
        subpixelPositioning = true
    }

    val markerElement = MarkerElement(
        marker = maplibregl.Marker(
            options = options
        ),
        element = element,
        bearingElement = bearingElement
    )
    markerElement.setBearing(position.bearing ?: 0f)
    return markerElement
}

fun createStops(stops: List<TransitStop>) {
    val zoom = geoMap.getZoom()
    val opacity = if (zoom >= STOP_ZOOM) 1f else 0f
    stops.forEach { stop ->
        val element = document.createDiv()
        element.className = "map-marker"

        val icon = document.createDiv()
        icon.className = "map-marker-icon transit-stop"
        element.appendChild(icon)

        val marker = maplibregl.Marker(
            options = jsObject {
                this.element = element
            }
        )
        marker.setLngLat(maplibregl.LngLat(stop.longitude, stop.latitude))
        marker.addTo(geoMap)

        // road blocked: can't get marker events to work
//        marker.setEventedParent(geoMap)
//        console.log(marker.listens("zoomend"))
//        marker.on("zoomend") {
//            println("ey: $it")
//            val show = geoMap.getZoom() >= 14
//            marker.getElement().style.display = if (show) "block" else "none"
//        }

        val me = MarkerElement(marker, element, null)
        me.setOpacity(opacity)
        stopElements.add(me)
    }
}

fun Position.toLngLat() = maplibregl.LngLat(longitude.toDouble(), latitude.toDouble())

fun maplibregl.Marker.move(
    origin: maplibregl.LngLat,
    destination: maplibregl.LngLat,
    durationMs: Double = 1000.0,
) {
    var start: Double? = null

    fun tick(time: Double) {
        if (start == null) start = time
        val t = ((time - start) / durationMs).coerceIn(0.0, 1.0)
        setLngLat(origin.interpolateTo(destination, t))
        if (t < 1.0) window.requestAnimationFrame(::tick)
    }

    window.requestAnimationFrame(::tick)
}

fun maplibregl.LngLat.interpolateTo(dest: maplibregl.LngLat, t: Double): maplibregl.LngLat =
    maplibregl.LngLat(
        lng = lng + (dest.lng - lng) * t,
        lat = lat + (dest.lat - lat) * t
    )