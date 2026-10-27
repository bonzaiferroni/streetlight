package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import streetlight.model.data.EventTag
import streetlight.model.io.ApiClient
import kotlin.time.Duration.Companion.seconds

/**
 * The events map: its [markerMap], filled view by view by its [cache], and the tag and search that filter it. The
 * cache starts over when the tag changes, or the search once it rests. The cousin of the web's `Earth`.
 */
class EarthModel(api: ApiClient, geoMap: GeoMap) : ViewModel() {
    private val store = storeOf(EarthState())

    val markerMap = MarkerMap(geoMap)
    val cache = EarthCache(viewModelScope, api, geoMap, markerMap)

    val tagState = store.mutableTapOf({ it.tag }) { copy(tag = it) }
    val searchState = store.mutableTapOf({ it.search }) { copy(search = it) }
    val isMovingState = geoMap.camera.cameraState.tapOf { it.isMoving }

    init {
        viewModelScope.launch { tagState.flow.drop(1).collectLatest { restart() } }
        viewModelScope.launch {
            searchState.flow.drop(1).collectLatest {
                delay(SearchRest)
                restart()
            }
        }
    }

    private fun restart() {
        val state = store.now
        cache.restart(state.tag, state.search.trim().takeIf { it.isNotEmpty() })
    }
}

/** The tag and search that filter the events map. */
data class EarthState(
    val tag: EventTag? = null,
    val search: String = "",
)

/** The time a search rests before the map starts over, as the web's. */
private val SearchRest = 1.seconds
