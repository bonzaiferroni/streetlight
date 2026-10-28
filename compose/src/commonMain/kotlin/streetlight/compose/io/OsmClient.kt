package streetlight.compose.io

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import kampfire.model.GeoPoint
import kampfire.model.GeoRect
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kotlinx.serialization.json.Json
import streetlight.model.external.OSMLocation

// acceptable use policy: https://operations.osmfoundation.org/policies/nominatim/
// docs: https://nominatim.org/release-docs/develop/api/Search/

/**
 * Calls OpenStreetMap's search directly, outside the Streetlight API, naming the app as its policy asks. The cousin of
 * the web's `OSMClient`.
 */
class OsmClient {
    private val client = HttpClient(CIO)

    /** The place at [point]. */
    suspend fun readLocationAt(point: GeoPoint): Outcome<OSMLocation> =
        read("$BaseUrl/reverse?lat=${point.lat}&lon=${point.lng}&format=jsonv2&addressdetails=1&extratags=1")

    /** The places matching [query], narrowed by [city] or [bounds]. */
    suspend fun readLocations(query: String, city: String? = null, bounds: GeoRect? = null): Outcome<List<OSMLocation>> {
        val text = when (city?.takeIf { it.isNotBlank() }) {
            null -> query
            else -> "$query, $city"
        }
        val params = listOfNotNull(
            "q=${text.encodeURLParameter()}",
            bounds?.let { "viewbox=${it.sw.lng},${it.ne.lat},${it.ne.lng},${it.sw.lat}" },
            bounds?.let { "bounded=1" },
            "format=jsonv2",
            "addressdetails=1",
            "extratags=1",
            "limit=10",
        ).joinToString("&")
        return read("$BaseUrl/search?$params")
    }

    private suspend inline fun <reified T> read(url: String): Outcome<T> = runCatching {
        val response = client.get(url) {
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.UserAgent, UserAgent)
        }
        if (!response.status.isSuccess()) return Problem("OpenStreetMap answered ${response.status.value}")
        Ok(OsmJson.decodeFromString<T>(response.bodyAsText()))
    }.getOrElse { Problem("OpenStreetMap could not be reached") }
}

private const val BaseUrl = "https://nominatim.openstreetmap.org"

/** The app as OpenStreetMap's policy asks it to be named. */
private const val UserAgent = "Streetlight app (https://streetlight.ing)"

private val OsmJson = Json { ignoreUnknownKeys = true }
