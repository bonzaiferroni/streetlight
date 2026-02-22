package streetlight.web

import kampfire.model.GeoPoint
import koala.utils.jsonConfig
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import kotlin.js.json

class OSMClient() {
    suspend fun readPlace(point: GeoPoint): OSMPlace? {
        // acceptable use policy: https://operations.osmfoundation.org/policies/nominatim/
        val url = "https://nominatim.openstreetmap.org/reverse" +
                    "?lat=${point.lat}&lon=${point.lng}&format=json"

        val response = window.fetch(url, RequestInit(headers = headers)).await()

        return response.tryDecodeText()
    }

    suspend fun readPlace(query: OSMQuery): List<OSMPlace>? {
        val url = "https://nominatim.openstreetmap.org/search?" + query.toQuery()

        val response = window.fetch(url, RequestInit(headers = headers)).await()

        return response.tryDecodeText(true)
    }
}

private val headers = json(
    "Accept" to "application/json",
    "User-Agent" to "Streetlight/1.0" //  (contact: you@example.com)
)