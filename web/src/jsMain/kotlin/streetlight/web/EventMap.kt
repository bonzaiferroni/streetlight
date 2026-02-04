package streetlight.web

import kotlinx.coroutines.CoroutineScope

class EventMap(
    scope: CoroutineScope,
    private val client: ClientContext,
): BrowserModel<StreetMapState>(StreetMapState(), scope) {

}

//data class EventMapState(
//
//)