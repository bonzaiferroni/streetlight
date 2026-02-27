package streetlight.web

import koala.dom.UIMessage
import koala.dom.set
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.EventParse
import streetlight.model.data.ReadEventRequest
import kotlin.time.Duration.Companion.seconds

class EventReader(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val route: ReadEventRoute,
) {
    val state = storeOf(EventRelayState())
    val message = storeOf(UIMessage(intro))

    init {
        if (route.link != null) {
            console.log("auto reading link")
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
            val parse = api.readEventFromUrl(ReadEventRequest(url, isImage))
            messageStream.cancel()
            val events = parse?.events?.takeIf { it.isNotEmpty() }
            if (events != null) {
                message.set("Finished. Are any of these the event you wish to post?")
                state.set { it.copy(parse = parse) }
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
    val parse: EventParse? = null,
    val stage: Int = 0,
    val completed: Set<Int> = emptySet()
)

private const val intro = "Share information about upcoming events."