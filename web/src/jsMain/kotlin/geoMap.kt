@file:Suppress("UnsafeCastFromDynamic")

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.fetch.Response
import kotlin.js.Promise

private val scope = MainScope()

// Provided by yer JS libs
external var geoMap: dynamic
external class maplibregl {
    class Marker(options: dynamic) {
        fun setLngLat(lngLat: Array<Double>): Marker
        fun addTo(map: dynamic): Marker
        fun remove()
    }
}

fun main() {
    document.addEventListener("DOMContentLoaded", {
        // initProto()
        console.log("ey!")
    })
}

//private fun initProto() {
//    scope.launch {
//        // Fetch the protobuf binary (gtfs-realtime FeedMessage)
//        val response: Response = window.fetch("/proxy/vehicle-position.pb").await()
//        val buffer = response.arrayBuffer().await()
//        val bytes = Int8Array(buffer) // works as byte-ish view for many decoders
//
//        // Decode using generated Kotlin protobuf classes (recommended)
//        val feed = transit_realtime.FeedMessage.parseFrom(bytes.toByteArray())
//
//        val buses = feed.entityList
//            .asSequence()
//            .filter { e ->
//                e.hasVehicle() &&
//                        e.vehicle.hasTrip() &&
//                        e.vehicle.trip.routeId == "15L" &&
//                        e.vehicle.hasPosition()
//            }
//            .map { Bus(it.vehicle) }
//            .toList()
//
//        val map = geoMap ?: return@launch
//
//        if (map.markers == undefined) {
//            map.markers = arrayOf<dynamic>()
//        }
//
//        // Remove old markers
//        (map.markers as Array<dynamic>).forEach { m -> m.remove() }
//        map.markers = arrayOf<dynamic>()
//
//        buses.forEach { bus ->
//            val element = document.createElement("div") as HTMLElement
//            element.className = "geo-marker"
//
//            val icon = document.createElement("div") as HTMLElement
//            val arrow = document.createElement("div") as HTMLElement
//            icon.className = "geo-icon bus-icon"
//            arrow.className = "bus-arrow"
//
//            val bearing = (bus.bearing - 90 + 360) % 360
//            arrow.style.setProperty("--bearing", "${bearing}deg")
//
//            element.appendChild(arrow)
//            element.appendChild(icon)
//
//            val marker = maplibregl.Marker(
//                js("""
//                    ({
//                        element: element,
//                        rotationAlignment: "map"
//                    })
//                """)
//            )
//                .setLngLat(arrayOf(bus.position.longitude, bus.position.latitude))
//                .addTo(map)
//
//            val arr = (map.markers as Array<dynamic>).toMutableList()
//            arr.add(marker)
//            map.markers = arr.toTypedArray()
//        }
//    }
//}
//
//private class Bus(private val data: transit_realtime.VehiclePosition) {
//    val position: transit_realtime.Position
//        get() = data.position
//
//    val bearing: Int
//        get() = if (position.hasBearing()) position.bearing.toInt() else 0
//}

/** Helpers **/
external class Int8Array(buffer: dynamic) {
    fun toByteArray(): ByteArray
}