package streetlight.web

import kampfire.model.GeoPoint
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import streetlight.model.Api
import streetlight.model.data.NewLocation
import kotlin.js.json

class OSMClient(app: AppContext): AppContext by app {
    suspend fun readPlaceInfo(point: GeoPoint): OSMPlace {
        // acceptable use policy: https://operations.osmfoundation.org/policies/nominatim/
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
        return jsonConfig.decodeFromString(text)
    }
}