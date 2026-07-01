package streetlight.web.io

import kampfire.model.Outcome
import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import streetlight.model.external.OSMLocation
import streetlight.model.external.OSMQuery
import kotlin.js.json

// acceptable use policy: https://operations.osmfoundation.org/policies/nominatim/
// docs: https://nominatim.org/release-docs/develop/api/Search/

class OSMClient() {
    suspend fun readLocationAt(point: GeoPoint): Outcome<OSMLocation>? {
        val url = "https://nominatim.openstreetmap.org/reverse" +
                    "?lat=${point.lat}&lon=${point.lng}&format=jsonv2&addressdetails=1&extratags=1"

        val response = window.fetch(url, RequestInit(headers = headers)).await()

        return response.tryDecodeTextResponse()
    }

    suspend fun readLocations(query: OSMQuery): Outcome<List<OSMLocation>>? {
        val url = "https://nominatim.openstreetmap.org/search?" + query.toQuery()

        val response = window.fetch(url, RequestInit(headers = headers)).await()

        return response.tryDecodeTextResponse()
    }

    suspend fun readLocations(query: String, city: String? = null, bounds: GeoBounds? = null): Outcome<List<OSMLocation>>? {
        val query = when (city?.takeIf { it.isNotBlank() }) {
            null -> query
            else -> "$query, $city"
        }
        val params = listOfNotNull(
            "q=${encodeURIComponent(query)}",
            bounds?.let {
                val sw = it.sw
                val ne = it.ne
                "viewbox=${sw.lng},${ne.lat},${ne.lng},${sw.lat}"
            },
            bounds?.let { "bounded=1" },
            "format=jsonv2",
            "addressdetails=1",
            "extratags=1",
            "limit=10"
        ).joinToString("&")

        val url = "https://nominatim.openstreetmap.org/search?$params"

        val response = window.fetch(url, RequestInit(headers = headers)).await()

        return response.tryDecodeTextResponse()
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

    bounds?.let {
        val sw = it.sw
        val ne = it.ne
        "viewbox=${sw.lng},${ne.lat},${ne.lng},${sw.lat}"
    },
    bounds?.let { "bounded=1" },

    "format=jsonv2",
    "addressdetails=1",
    "extratags=1",
    "limit=$limit"
).joinToString("&")

external fun encodeURIComponent(s: String): String