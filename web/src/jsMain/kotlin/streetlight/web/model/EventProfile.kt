package streetlight.web.model

//class EventProfile(
//    scope: CoroutineScope,
//    private val client: ClientContext,
//) : BrowserModel<EventProfileState>(EventProfileState(), scope) {
//
//    val eventFlow = stateFlow.mapDistinct { it.event }.filterNotNull()
//
//    fun fetchEvent(eventId: EventId) {
//        scope.launch {
//            val event = client.api.readEvent(eventId) ?: return@launch
//            setEvent(event)
//        }
//    }
//
//    fun setEvent(event: Event) {
//        setState { it.copy(event = event) }
//    }
//}
//
//data class EventProfileState(
//    val event: Event? = null
//)