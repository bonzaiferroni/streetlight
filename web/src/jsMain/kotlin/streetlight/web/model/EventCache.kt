package streetlight.web.model

//class EventCache(
//    private val scope: CoroutineScope,
//    private val config: SiteConfig,
//    private val api: ApiClient,
//) {
//    private val state = storeOf(EventCacheState())
//    val stateNow get() = state.now
//    val stateFlow = state.flow
//    val lightFlow = stateFlow.mapDistinct { it.lights }
//
//    private var localCache by jsonListStorageOf<EventLight>(EVENT_CACHE_KEY)
//
//    init {
//        scope.launch {
//            launch {
//                val lights = readLights()
//                state.set { it.copy(lights = lights) }
//            }
//            launch {
//                lightFlow.collect { lights ->
//                    val events = when (lights.isEmpty()) {
//                        true -> emptyList()
//                        else -> api.readEventLocations(lights.map { it.eventId }) ?: emptyList() // td: fail message
//                    }.sortedBy { it.startsAt }
//                    state.set { it.copy(events = events) }
//                }
//            }
//        }
//    }
//
//    fun editLight(star: EventLight) {
//        scope.launch {
//            val isSuccess = editStorage(star)
//            if (isSuccess) {
//                editState(star)
//            }
//        }
//    }
//
//    fun clear() {
//        val events = emptyList<EventLight>()
//        localCache = events
//        state.set { it.copy(lights = events)}
//    }
//
//    private suspend fun readLights() = when(config.stateNow.lightSync) {
//        true -> api.readEventStars() ?: emptyList()
//        else -> localCache
//    }
//
//    private suspend fun editStorage(star: EventLight): Boolean {
//        return when (config.stateNow.lightSync) {
//            true -> api.editEventStar(star) ?: false
//            else -> editLocalCache(star)
//        }
//    }
//
//    private fun editLocalCache(star: EventLight): Boolean {
//        localCache = modifyStateEvents(star)
//        return true
//    }
//
//    private fun modifyStateEvents(star: EventLight) = when (star.value) {
//        null -> stateNow.lights.filter { it.eventId != star.eventId }
//        else -> (stateNow.lights + star)
//    }
//
//    private fun editState(star: EventLight) {
//        val events = modifyStateEvents(star)
//        state.set { it.copy(lights = events)}
//    }
//}
//
//data class EventCacheState(
//    val lights: List<EventLight> = emptyList(),
//    val events: List<EventLocation> = emptyList(),
//)

//const val EVENT_CACHE_KEY = "streetlight.event-cache"