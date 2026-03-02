package streetlight.web.model

import koala.dom.UIMessage
import koala.dom.set
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.MultiEventParseResponse
import streetlight.model.data.ParseRequest
import streetlight.web.ReadEventRoute
import streetlight.web.io.ApiClient
import streetlight.web.ui.loadingMessages
import kotlin.time.Duration.Companion.seconds

class EventReader(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val route: ReadEventRoute,
) {
    val state = storeOf(EventRelayState())
    val message = storeOf(UIMessage(intro))
    val location = storeOf(route.location)

    init {
        if (route.link != null) {
            state.set { it.copy(link = route.link) }
            readLink()
        }
    }

    fun setLink(value: String) {
        state.set { it.copy(link = value) }
    }

    fun readLink() = readUrl(state.now.link, false)

    fun startOver() {
        state.set { EventRelayState() }
    }

    fun setCompleted(index: Int) {
        state.set { it.copy(completed = it.completed + index) }
    }

    fun postAll() {
        val location = location.now ?: return
        val events = state.now.parse?.events ?: return
        val link = state.now.link
        scope.launch {
            events.forEachIndexed { index, event ->
                if (state.now.getStatus(index) == 200) return@forEachIndexed
                val edit = event.takeIf { it.isValid } ?: return@forEachIndexed
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

    private fun readUrl(url: String?, isImage: Boolean) {
//        state.set {
//            val time = tomorrowNoon().toLocalDateTime()
//            it.copy(stage = 1, parse = EventParse(true, listOf(
//                EventParseItem("event 1", time.time, time.date),
//                EventParseItem("event 2", time.time, time.date)
//            )))
//        }

        val url = url?.takeIf { it.isNotEmpty() } ?: return
        state.set { it.copy(stage = 1)}
        scope.launch {
            val messageStream = launch {
                message.set("Reading url...")
                while(true) {
                    delay(3.seconds)
                    message.set(loadingMessages.random())
                }
            }
            val parse = api.parseMultiEventFromUrl(ParseRequest(url, isImage))
            messageStream.cancel()
            val events = parse?.events?.takeIf { it.isNotEmpty() }
            if (events != null) {
                message.set("Finished. Are any of these the event you wish to post?")
                state.set { it.copy(parse = parse, completed = MutableList(events.size) { null }) }
            } else {
                message.set("I couldn't find any events at that link. It might be for human readers only.")
            }
        }
    }
}

data class EventRelayState(
    val text: String = "",
    val link: String = "",
    val imageUrl: String = "",
    val parse: MultiEventParseResponse? = null,
    val stage: Int = 0,
    val completed: List<Int?> = emptyList()
) {
    fun getStatus(index: Int) = completed.getOrNull(index)
}

private const val intro = "Share information about upcoming events."