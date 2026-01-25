package streetlight.web

import kotlinx.coroutines.CoroutineScope

interface AppContext {
    val client: ClientContext
    val navigator: AppNavigator
    val home: Home
}

interface Home {
    val gtfsMap: GtfsMap
    val eventMap: EventMap
}

interface ClientContext {
    val gtfs: GtfsBrowserClient
    val event: EventBrowserClient
}