@file:Suppress("UnsafeCastFromDynamic")

package streetlight.web

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8Array
import kotlin.js.Date
import kotlin.time.Duration.Companion.seconds

private val scope = MainScope()
private val buses = mutableMapOf<String, Bus>()
external var geoMap: maplibregl.Map
var timestamp = 0L

@OptIn(ExperimentalJsExport::class)
@JsExport
fun addRtdToMap() {
    scope.launch {
        val feedType = loadFeedType()
        while (true) {
            val vehicles = fetchVehicles(feedType, setOf("15L", "15", "121", "121L", "107R", "101H"))
            val currentTime = Date.now().toLong() / 1000

            vehicles?.forEach { vehicle ->
                val position = vehicle.position ?: return@forEach
                val vehicleId = vehicle.vehicle?.id ?: return@forEach

                val bus = if (buses.containsKey(vehicleId)) {
                    val bus = buses.getValue(vehicleId)
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
                    val bus = createBus(position)
                    bus.marker.setLngLat(position.toLngLat())
                        .addTo(geoMap)
                    buses[vehicleId] = bus
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
                val missingIds = buses.map { it.key }
                    .filter { vehicleId -> vehicles.none { it.vehicle?.id == vehicleId} }
                missingIds.forEach {
                    buses[it]?.marker?.remove()
                    buses.remove(it)
                }
            }

            delay(30.seconds)
        }
    }
}

suspend fun loadFeedType(): ProtobufType {
    val root = protobuf.load("/static/proto/gtfs-realtime.proto").await()
    return root.lookupType("transit_realtime.FeedMessage")
}

suspend fun fetchVehicles(feedType: ProtobufType, routeIds: Set<String>): List<VehiclePosition>? {
    val response = window.fetch("/gtfs/vehicle-position.pb").await()
        .arrayBuffer().await()
    val buffer = Uint8Array(response)

    val feed = feedType.decode<FeedEntity>(buffer)
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

fun createBus(position: Position): Bus {
    val element = document.createDiv()
    element.className = "map-marker"

    val bearingElement = document.createDiv()
    bearingElement.className = "bus-bearing"
    element.appendChild(bearingElement)

    val icon = document.createDiv()
    icon.className = "map-marker-icon bus-icon"
    element.appendChild(icon)

    val bus = Bus(
        marker = maplibregl.Marker(
            options = jsObject {
                this.element = element
                subpixelPositioning = true
            }
        ),
        element = element,
        bearingElement = bearingElement
    )
    bus.setBearing(position.bearing ?: 0f)
    return bus
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