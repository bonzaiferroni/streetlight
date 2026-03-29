package streetlight.web.model

import koala.dom.jsonListStorageOf
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.EventLocation
import streetlight.model.data.EventStar
import streetlight.web.io.ApiClient

class EventCache(
    private val scope: CoroutineScope,
    private val config: SiteConfig,
    private val api: ApiClient,
) {

    private val state = storeOf(EventCacheState())
    val stateNow get() = state.now
    val stateFlow = state.flow
    val starFlow = stateFlow.mapDistinct { it.stars }

    private var localCache by jsonListStorageOf<EventStar>(EVENT_CACHE_KEY)

    init {
        scope.launch {
            launch {
                val stars = readStars()
                state.set { it.copy(stars = stars) }
            }
            launch {
                starFlow.collect { stars ->
                    val events = when (stars.isEmpty()) {
                        true -> emptyList()
                        else -> api.readEventLocations(stars.map { it.eventId }) ?: emptyList() // td: fail message
                    }.sortedBy { it.startsAt }
                    state.set { it.copy(events = events) }
                }
            }
        }
    }

    fun editStar(star: EventStar) {
        scope.launch {
            val isSuccess = editStorage(star)
            if (isSuccess) {
                editState(star)
            }
        }
    }

    fun clear() {
        val events = emptyList<EventStar>()
        localCache = events
        state.set { it.copy(stars = events)}
    }

    private suspend fun readStars() = when(config.stateNow.starSync) {
        true -> api.readEventStars() ?: emptyList()
        else -> localCache
    }

    private suspend fun editStorage(star: EventStar): Boolean {
        return when (config.stateNow.starSync) {
            true -> api.editEventStar(star) ?: false
            else -> editLocalCache(star)
        }
    }

    private fun editLocalCache(star: EventStar): Boolean {
        localCache = modifyStateEvents(star)
        return true
    }

    private fun modifyStateEvents(star: EventStar) = when (star.value) {
        null -> stateNow.stars.filter { it.eventId != star.eventId }
        else -> (stateNow.stars + star)
    }

    private fun editState(star: EventStar) {
        val events = modifyStateEvents(star)
        state.set { it.copy(stars = events)}
    }
}

data class EventCacheState(
    val stars: List<EventStar> = emptyList(),
    val events: List<EventLocation> = emptyList(),
)

const val EVENT_CACHE_KEY = "streetlight.event-cache"