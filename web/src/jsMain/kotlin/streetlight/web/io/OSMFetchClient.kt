package streetlight.web.io

import kampfire.model.GeoPoint
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import streetlight.model.external.OSMPlace
import streetlight.model.external.OSMQuery
import kotlin.js.json

// acceptable use policy: https://operations.osmfoundation.org/policies/nominatim/
// docs: https://nominatim.org/release-docs/develop/api/Search/

class OSMFetchClient() {
    suspend fun readPlace(point: GeoPoint): OSMPlace? {
        val url = "https://nominatim.openstreetmap.org/reverse" +
                    "?lat=${point.lat}&lon=${point.lng}&format=jsonv2&addressdetails=1&extratags=1"

        val response = window.fetch(url, RequestInit(headers = headers)).await()

        return response.tryDecodeText()
    }

    suspend fun readPlaces(query: OSMQuery): List<OSMPlace>? {
        val url = "https://nominatim.openstreetmap.org/search?" + query.toQuery()

        val response = window.fetch(url, RequestInit(headers = headers)).await()

        return response.tryDecodeText(true)
    }

    suspend fun readPlaces(freeForm: String, limit: Int = 10): List<OSMPlace>? {
        val url = "https://nominatim.openstreetmap.org/search?" + listOf(
            "q=${encodeURIComponent(freeForm)}",
            "format=jsonv2",
            "addressdetails=1",
            "extratags=1",
            "limit=${limit}"
        ).joinToString("&")

        val response = window.fetch(url, RequestInit(headers = headers)).await()

        return response.tryDecodeText(true)
    }
}

private val headers = json(
    "Accept" to "application/json",
    "User-Agent" to "Streetlight/1.0" //  (contact: you@example.com)
)

fun OSMQuery.toQuery() = listOfNotNull(
    amenity?.let { "amenity=${encodeURIComponent(it)}" },
    street?.let { "street=${encodeURIComponent(it)}" },
    city?.let { "city=${encodeURIComponent(it)}" },
    county?.let { "county=${encodeURIComponent(it)}" },
    state?.let { "state=${encodeURIComponent(it)}" },
    country?.let { "country=${encodeURIComponent(it)}" },
    postalcode?.let { "postalcode=${encodeURIComponent(it)}" },
    "format=jsonv2",
    "addressdetails=1",
    "extratags=1",
    "limit=$limit"
).joinToString("&")

external fun encodeURIComponent(s: String): String