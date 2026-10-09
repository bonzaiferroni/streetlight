package koala.model

import kampfire.model.GeoPoint
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import web.geolocation.GeolocationPosition
import web.geolocation.GeolocationPositionError
import web.geolocation.getCurrentPosition
import web.navigator.navigator

/** The problems of reading the device's location. */
object LocationProblem {
    val Unavailable = Problem("Your location is unavailable")
}

/** The device's current location, or a [Problem] when the browser cannot or may not provide it. */
suspend fun readCurrentLocation(): Outcome<GeoPoint> {
    val position = try {
        navigator.geolocation.getCurrentPosition()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        return LocationProblem.Unavailable
    }
    return Ok(position.toGeoPoint())
}

/**
 * The device's location each time it moves.
 *
 * Losing access to the location ends the flow with a [Problem]. A position the browser could not fix is skipped.
 */
fun streamCurrentLocation(): Flow<Outcome<GeoPoint>> = callbackFlow {
    val geolocation = navigator.geolocation
    val watchId = geolocation.watchPositionWithCallbacks(
        successCallback = { trySend(Ok(it.toGeoPoint())) },
        errorCallback = { error ->
            if (error.code == GeolocationPositionError.PERMISSION_DENIED) {
                trySend(LocationProblem.Unavailable)
                close()
            }
        },
    )
    awaitClose { geolocation.clearWatch(watchId) }
}

private fun GeolocationPosition.toGeoPoint() = GeoPoint(coords.longitude, coords.latitude)
