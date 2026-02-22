package streetlight.web

import koala.dom.UIMessage
import koala.dom.set
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.EventParse
import streetlight.model.data.EventParseItem
import kotlin.time.Duration.Companion.seconds

class EventParser(
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    val state = storeOf(EventParserState())
    val message = storeOf(UIMessage())
    val selectionFlow = MutableSharedFlow<List<EventParseItem>>()

    fun select(index: Int) {
        val item = state.now.parse?.events?.getOrNull(index) ?: return
        scope.launch {
            selectionFlow.emit(listOf(item))
            state.set { it.copy(isOpen = false) }
        }
    }

    fun readUrl(url: String?) {
        val url = url?.takeIf { it.startsWith("http") } ?: return
        state.set { it.copy(isOpen = true, url = url, parse = null) }
        scope.launch {
            val messageStream = launch {
                message.set("Reading url...")
                while(true) {
                    delay(3.seconds)
                    message.set(loadingMessages.random())
                }
            }
            val parse = api.readEventFromUrl(url)
            messageStream.cancel()
            val events = parse?.events
            if (events != null) {
                message.set("Finished. Are any of these the event you wish to post?")
            } else {
                message.set("I couldn't find any events at that link. It might be for human readers only.")
            }
            state.set { it.copy(parse = parse) }
        }
    }
}

data class EventParserState(
    val isOpen: Boolean = false,
    val url: String = "",
    val parse: EventParse? = null
)

private val loadingMessages = listOf(
    "Reticulating splines...",
    "Choosing favorite span...",
    "Fleeing shadow DOM...",
    "Alternating universes...",
    "Reoptimizing search engine...",
    "Demystifying class architecture...",
    "Shuffling parameters...",
    "Compiling generic arguments...",
    "Breaking in non-breaking space...",
    "Dividing by zero...",
    "Double clicking on hyperlinks...",
    "Allocating courage...",
    "Calculating sum of NaN...",
    "Waving to garbage collector...",
    "Incrementing indices...",
    "Obfuscating browser history..."
)