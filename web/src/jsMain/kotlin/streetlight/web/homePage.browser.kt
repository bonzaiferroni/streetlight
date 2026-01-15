@file:Suppress("UnsafeCastFromDynamic")

package streetlight.web

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLElement
import org.w3c.fetch.Response

private val scope = MainScope()
private val markers = mutableListOf<maplibregl.Marker>()

fun main() {
    document.addEventListener("DOMContentLoaded", {
        initProto()
    })
}

private fun initProto() {
    scope.launch {
        val root = protobuf.load("/static/proto/gtfs-realtime.proto").await()
        val feedType = root.lookupType("transit_realtime.FeedMessage")

        val response: Response = window.fetch("/proxy/vehicle-position.pb").await()
        val buffer = Uint8Array(response.arrayBuffer().await())

        val feed = feedType.decode<FeedEntity>(buffer)

        val vehicles = feed.entity
            .mapNotNull { it.vehicle }
            .filter { it.trip != null && it.trip.routeId == "15L" }
            .toList()

        // Remove old streetlight.web.streetlight.web.markers
        markers.clear()

        vehicles.forEach { vehicle ->
            val position = vehicle.position ?: return@forEach
            var element = document.createElement("div") as HTMLElement
            element.className = "map-marker"

            val icon = document.createElement("div") as HTMLElement
            icon.className = "map-marker-icon bus-icon"
            element.appendChild(icon)

            val bearing = position.bearing
            if (bearing != null) {
                val bearing = (bearing - 90 + 360) % 360
                val arrow = document.createElement("div") as HTMLElement
                arrow.className = "bus-arrow"
                arrow.style.setProperty("--bearing", "${bearing}deg")
                element.appendChild(arrow)
            }

            val options: dynamic = MarkerOptions(
                element = element,
                rotationAlignment = "map"
            )

            val obj: dynamic = js("{}")
            obj.element = element
            obj["element"] = element
            obj["rotationAlignment"] = "map"

            val marker = maplibregl.Marker(
                options = jsObject {
                    this.element = element
                    rotationAlignment = "map"
                }
            )
                .setLngLat(arrayOf(vehicle.position.longitude, vehicle.position.latitude))
                .addTo(geoMap)

            markers.add(marker)
        }
    }
}

external var geoMap: Map

fun jsObject(block: dynamic.() -> Unit): dynamic {
    val obj = js("{}")
    block(obj)
    return obj
}