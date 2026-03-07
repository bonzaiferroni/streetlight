package streetlight.web.model

import koala.dom.UIMessage
import koala.dom.set
import koala.model.storeOf
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import streetlight.model.data.HtmlParseRequest
import streetlight.model.data.ImageParseRequest
import streetlight.model.data.Location
import streetlight.model.data.MultiEventParseResponse
import streetlight.model.data.ParseRequest
import streetlight.model.data.UrlParseRequest
import streetlight.web.EventScoutRoute
import streetlight.web.ui.ViewModel

class EventScout(
    private val scope: CoroutineScope,
    private val route: EventScoutRoute,
    override val app: AppContext
): ViewModel {
    private val state = storeOf(EventScoutState())
    private val message = storeOf(UIMessage(intro))
    val stateFlow = state.flow
    val messageFlow = message.flow

    init {
        if (route.location != null) {
            state.set { it.copy(location = route.location) }
        } else if (route.link != null) {
            state.set { it.copy(link = route.link) }
            readLink()
        }
    }

    fun setLink(value: String) {
        state.set { it.copy(link = value) }
    }

    fun readLink() = readUrl(state.now.link, false)

    fun startOver() {
        state.set { EventScoutState() }
    }

    fun setCompleted(index: Int) {
        state.set { it.copy(completed = it.completed + index) }
    }

    fun postAll() {
        val location = state.now.location ?: return
        val events = state.now.parse?.events ?: return
        val link = state.now.link
        scope.launch {
            events.forEachIndexed { index, event ->
                if (state.now.getStatus(index) == 200) return@forEachIndexed
                val edit = event.copy(
                    locationId = location.locationId,
                ).takeIf { it.isValid } ?: return@forEachIndexed
                val response = api.createOrEditEvent(edit)
                if (response == null) {
                    console.log("unhandled error")
                    return@forEachIndexed
                }

                val completed = state.now.completed.toMutableList()
                completed[index] = response.status
                state.set { it.copy(completed = completed) }
            }
        }
    }

    fun readCalendar() {
        readUrl(state.now.location?.eventsLink, false)
    }

    fun setHtmlUrl(value: String) {
        state.set { it.copy(htmlUrl = value) }
    }

    fun readHtml() {
        val blobUrl = state.now.htmlUrl ?: return
        scope.launch {
            val response = window.fetch(blobUrl).await()
            val html = response.text().await()
            val request = HtmlParseRequest("null", html)
            sendRequest(request)
        }
    }

    private fun readUrl(url: String?, isImage: Boolean) {
        val url = url?.takeIf { it.isNotEmpty() } ?: return
        scope.launch {
            message.set("Reading url, this can take a minute.")
            val request = if (isImage) {
                ImageParseRequest(url)
            } else {
                UrlParseRequest(url)
            }
            sendRequest(request)
        }
    }

    private suspend fun sendRequest(request: ParseRequest) {
        val parse = api.parseMultiEvent(request)
        val events = parse?.events?.takeIf { it.isNotEmpty() }
        if (events != null) {
            message.set("Finished. Are any of these the event you wish to post?")
            state.set { it.copy(parse = parse, completed = MutableList(events.size) { null }) }
        } else {
            message.set("I couldn't find any events in the content served from that link. " +
                    "It might be for human readers only.")
        }
    }
}

data class EventScoutState(
    val text: String = "",
    val link: String = "",
    val imageUrl: String = "",
    val parse: MultiEventParseResponse? = null,
    val completed: List<Int?> = emptyList(),
    val location: Location? = null,
    val htmlUrl: String? = null,
) {
    fun getStatus(index: Int) = completed.getOrNull(index)
}

private const val intro = "Share information about upcoming events. " +
        "You may provide a link, image, or text and we'll do our best to understand it. " +
        "You'll have a chance to review the information before it is posted."