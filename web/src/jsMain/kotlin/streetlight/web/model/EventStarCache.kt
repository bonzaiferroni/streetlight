package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import koala.utils.jsonConfig
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.get
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.EventStar
import streetlight.web.io.ApiClient

class EventStarCache(
    private val scope: CoroutineScope,
    private val config: SiteConfig,
    private val api: ApiClient,
) {

    private val state = storeOf(StarCacheState())
    val stateNow get() = state.now
    val stateFlow = state.flow
    val starFlow = stateFlow.mapDistinct { it.stars }

    private var localCache
        get() = localStorage[EVENT_STAR_CACHE_KEY]?.let {
            console.log(it)
            jsonConfig.decodeFromString<List<EventStar>>(it)
        } ?: emptyList()
        set(value: List<EventStar>) {
            val json = jsonConfig.encodeToString(value)
            localStorage.setItem(EVENT_STAR_CACHE_KEY, json)
        }

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
                    }
                    state.set { it.copy(events = events) }
                }
            }
        }
    }

    fun editStar(star: EventStar) {
        scope.launch {
            editEvent(star)
            setStateEvents(star)
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

    private suspend fun editEvent(star: EventStar) {
        when (config.stateNow.starSync) {
            true -> api.editEventStar(star)
            else -> editLocalCache(star)
        }
    }

    private fun editLocalCache(star: EventStar) {
        localCache = modifyStateEvents(star)
    }

    private fun modifyStateEvents(star: EventStar) = when (star.value) {
        null -> stateNow.stars.filter { it.eventId != star.eventId }
        else -> stateNow.stars + star
    }

    private fun setStateEvents(star: EventStar) {
        val events = modifyStateEvents(star)
        state.set { it.copy(stars = events)}
    }
}

data class StarCacheState(
    val stars: List<EventStar> = emptyList(),
    val events: List<EventLocation> = emptyList(),
)

const val EVENT_STAR_CACHE_KEY = "event-star-cache"