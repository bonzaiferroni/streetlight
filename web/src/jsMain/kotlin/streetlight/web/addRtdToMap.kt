@file:Suppress("UnsafeCastFromDynamic")

package streetlight.web

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLElement
import kotlin.time.Duration.Companion.minutes

private val scope = MainScope()
private val buses = mutableMapOf<String, Bus>()
external var geoMap: maplibregl.Map

@OptIn(ExperimentalJsExport::class)
@JsExport
fun addRtdToMap() {
    scope.launch {
        val feedType = loadFeedType()
        while (true) {
            console.log("fetching vehicles")
            val vehicles = fetchVehicles(feedType, setOf("15L", "15", "121", "121L", "107R", "101H", "228A"))

            vehicles?.forEach { vehicle ->
                val position = vehicle.position ?: return@forEach
                val tripId = vehicle.trip?.tripId ?: return@forEach

                if (buses.containsKey(tripId)) {
                    val bus = buses.getValue(tripId)
                    val current = bus.marker.getLngLat()
                    val destination = position.toLngLat()
                    val distance = current.distanceTo(destination)
                    if (distance > 1) {
                        bus.marker.move(current, destination)
                    } else {
                        bus.marker.setLngLat(position.toLngLat())
                    }
                    bus.setBearing(position.bearing ?: 0f)
                } else {
                    val bus = createBus(position)
                    bus.marker.setLngLat(position.toLngLat())
                        .addTo(geoMap)
                    buses[tripId] = bus
                }
            }

            delay(1.minutes)
        }
    }
}

suspend fun loadFeedType(): ProtobufType {
    val root = protobuf.load("/static/proto/gtfs-realtime.proto").await()
    return root.lookupType("transit_realtime.FeedMessage")
}

var stamp = 0L

suspend fun fetchVehicles(feedType: ProtobufType, routeIds: Set<String>): List<VehiclePosition>? {
    val response = window.fetch("/proxy/vehicle-position.pb").await()
        .arrayBuffer().await()
    val buffer = Uint8Array(response)

    val feed = feedType.decode<FeedEntity>(buffer)
    // console.log(feed.entity.mapNotNull { it.vehicle?.trip?.routeId }.toSet() .joinToString(", "))
    val timestamp = feed.header.timestamp
    val delta = timestamp - stamp
    console.log("timestamp: $timestamp stamp: $stamp delta: $delta deltaInt: ${delta.toInt()}")
    stamp = timestamp
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

data class Bus(
    val marker: maplibregl.Marker,
    val bearingElement: HTMLElement,
) {
    var lastBearing = 0f

    fun setBearing(bearing: Float) {
        val delta = ((bearing - lastBearing + 540) % 360) - 180;
        lastBearing += delta
        val adjusted = lastBearing - 90
        bearingElement.style.setProperty("--bearing", "${adjusted}deg")
    }
}