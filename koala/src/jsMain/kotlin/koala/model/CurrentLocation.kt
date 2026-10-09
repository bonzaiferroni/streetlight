package koala.model

import kampfire.model.GeoPoint
import kotlinx.coroutines.CancellationException
import web.geolocation.getCurrentPosition
import web.navigator.navigator

/** The device's current location, or `null` when the browser cannot or may not provide it. */
suspend fun readCurrentLocation(): GeoPoint? {
    val position = try {
        navigator.geolocation.getCurrentPosition()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        return null
    }
    return GeoPoint(position.coords.longitude, position.coords.latitude)
}
