package streetlight.web

import kampfire.model.GeoPoint
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import kotlin.js.json

class LocationBrowserClient {
    suspend fun readPlaceInfo(point: GeoPoint): NominatimPlace {
        val url =
            "https://nominatim.openstreetmap.org/reverse" +
                    "?lat=${point.lat}&lon=${point.lng}&format=json"

        val response = window.fetch(
            url,
            RequestInit(
                headers = json(
                    "Accept" to "application/json",
                    "User-Agent" to "Streetlight/1.0" //  (contact: you@example.com)
                )
            )
        ).await()

        val text = response.text().await()
        return Json.decodeFromString(text)
    }

}