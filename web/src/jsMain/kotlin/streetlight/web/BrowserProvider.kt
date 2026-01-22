package streetlight.web

class BrowserProvider {
    val repo = BrowserRepository()
}

class BrowserRepository {
    val gtfsClient = GtfsBrowserClient()
    val eventClient = EventBrowserClient()
}