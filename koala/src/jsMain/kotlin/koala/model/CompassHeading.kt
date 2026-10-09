package koala.model

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import web.device.DEVICE_ORIENTATION_ABSOLUTE
import web.device.DeviceOrientationEvent
import web.events.addEventHandler
import web.screen.screen
import web.window.window

/**
 * The heading the top of the screen faces, in degrees clockwise from north, each time the device turns.
 *
 * Emits nothing where the browser has no absolute orientation.
 */
fun streamCompassHeading(): Flow<Float> = callbackFlow {
    val removeHandler = window.addEventHandler(DeviceOrientationEvent.DEVICE_ORIENTATION_ABSOLUTE) { event ->
        val alpha = event.alpha ?: return@addEventHandler
        trySend((360 - alpha + screen.orientation.angle).mod(360.0).toFloat())
    }
    awaitClose(removeHandler)
}
