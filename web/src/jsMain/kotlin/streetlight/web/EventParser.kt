package streetlight.web

import koala.dom.UIMessage
import koala.dom.set
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.ColdParse
import streetlight.model.data.EventParse
import streetlight.model.data.ParseRequest
import kotlin.time.Duration.Companion.seconds

class EventParser(
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    val state = storeOf(EventParserState())
    val message = storeOf(UIMessage())
    val selectionFlow = MutableSharedFlow<List<EventParse>>()

    fun select(index: Int) {
        val item = state.now.parse?.events?.getOrNull(index) ?: return
        scope.launch {
            selectionFlow.emit(listOf(item))
            state.set { it.copy(isOpen = false) }
        }
    }

    fun readUrl(url: String?, isImage: Boolean) {
        val url = url?.takeIf { it.isNotEmpty() } ?: return
        state.set { it.copy(isOpen = true, url = url, parse = null) }
        scope.launch {
            val messageStream = launch {
                message.set("Reading url...")
                while(true) {
                    delay(3.seconds)
                    message.set(loadingMessages.random())
                }
            }
            val parse = api.readEventFromUrl(ParseRequest(url, isImage))
            messageStream.cancel()
            val events = parse?.events?.takeIf { it.isNotEmpty() }
            if (events != null) {
                message.set("Finished. Are any of these the event you wish to post?")
            } else {
                message.set("I couldn't find any events at that link. It might be for human readers only.")
            }
            // state.set { it.copy(parse = parse) }
        }
    }
}

data class EventParserState(
    val isOpen: Boolean = false,
    val url: String = "",
    val parse: ColdParse? = null
)

