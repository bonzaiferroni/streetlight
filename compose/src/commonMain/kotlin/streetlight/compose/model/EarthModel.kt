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
import kampfire.model.Ok
import streetlight.model.data.Entity
import streetlight.model.data.EventGroup
import streetlight.model.data.EventTag
import streetlight.model.io.ApiClient
import kotlin.time.Duration.Companion.seconds

/**
 * The events map: its [markerMap], filled view by view by its [cache], the tag and search that filter it, and the
 * entities of the marker in focus. The cache starts over when the tag changes, or the search once it rests. A focused
 * location's events are read with the filters in place. The cousin of the web's `Earth`.
 */
class EarthModel(private val api: ApiClient, geoMap: GeoMap) : ViewModel() {
    private val store = storeOf(EarthState())

    val markerMap = MarkerMap(geoMap)
    val cache = EarthCache(viewModelScope, api, geoMap, markerMap)

    val tagState = store.mutableTapOf({ it.tag }) { copy(tag = it) }
    val searchState = store.mutableTapOf({ it.search }) { copy(search = it) }
    val isMovingState = geoMap.camera.cameraState.tapOf { it.isMoving }
    val focusEntitiesState = store.tapOf { it.focusEntities }

    init {
        viewModelScope.launch { tagState.flow.drop(1).collectLatest { restart() } }
        viewModelScope.launch {
            searchState.flow.drop(1).collectLatest {
                delay(SearchRest)
                restart()
            }
        }
        viewModelScope.launch { markerMap.focusState.flow.collectLatest { inflateFocus(it) } }
    }

    /** Sets the entities of [marker]: a location's events as the filters allow, or the marked entity itself. */
    private suspend fun inflateFocus(marker: MapMarker?) {
        val entity = marker?.entity
        val entities = when (entity) {
            null -> null
            // a failed read shows nothing, as the view has no toaster yet
            is EventGroup -> (api.earth.inflate(entity.locationId, store.now.tag, store.now.searchText) as? Ok)?.data
            else -> listOf(entity)
        }
        store.set { copy(focusEntities = entities) }
    }

    private fun restart() {
        val state = store.now
        cache.restart(state.tag, state.searchText)
    }
}

/** The tag and search that filter the events map, and the entities of the marker in focus. */
data class EarthState(
    val tag: EventTag? = null,
    val search: String = "",
    val focusEntities: List<Entity>? = null,
) {
    /** The search as a query reads it, `null` when blank. */
    val searchText get() = search.trim().takeIf { it.isNotEmpty() }
}

/** The time a search rests before the map starts over, as the web's. */
private val SearchRest = 1.seconds
