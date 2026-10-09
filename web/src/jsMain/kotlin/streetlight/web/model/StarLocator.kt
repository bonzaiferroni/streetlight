package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.Messenger
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import koala.model.GeoMap
import koala.model.readCurrentLocation
import koala.model.streamCurrentLocation
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import web.events.EventHandler
import web.events.addHandler
import web.navigator.navigator
import web.permissions.PermissionDescriptor
import web.permissions.PermissionName
import web.permissions.PermissionState
import web.permissions.PermissionStatus
import web.permissions.changeEvent
import web.permissions.geolocation
import web.permissions.granted
import web.permissions.query

/** The viewer's location on the map, known once they have granted the browser access to it. */
class StarLocator(
    private val scope: CoroutineScope,
    val geoMap: GeoMap,
) {
    private val state = storeOf(StarLocatorState())
    val startPointState = state.tapOf { it.starPoint }

    val starLocationLayer = geoMap.getOrCreateLayer(MarkerLayerConfig.StarLocation)

    init {
        scope.launch("StarLocator > permission") {
            val status = navigator.permissions.query(PermissionDescriptor(name = PermissionName.geolocation))
            readPermission(status)
            status.changeEvent.addHandler(EventHandler { readPermission(status) })
        }

        startPointState.reactIn(scope) { point ->
            starLocationLayer.setPoints(listOfNotNull(point?.let { StarMarker(it) }))
        }
    }

    /**
     * Follows the device's location until cancelled, or until access to it is lost, which it reports to [messenger].
     */
    suspend fun trackLocation(messenger: Messenger) {
        streamCurrentLocation().collect { outcome ->
            val point = outcome.toDataOr(messenger) { return@collect }
            state.set { copy(starPoint = point) }
        }
    }

    /** Reads the location when [status] grants access to it, and forgets it otherwise. */
    private fun readPermission(status: PermissionStatus) {
        if (status.state != PermissionState.granted) {
            state.set { copy(starPoint = null) }
            return
        }
        scope.launch("StarLocator > location") {
            val point = readCurrentLocation().toDataOrNull()
            state.set { copy(starPoint = point) }
        }
    }
}

data class StarLocatorState(
    val starPoint: GeoPoint? = null
)
