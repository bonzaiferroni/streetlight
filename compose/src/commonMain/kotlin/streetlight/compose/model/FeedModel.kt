package streetlight.compose.model

import kampfire.model.MutableTap
import kampfire.model.Ok
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import streetlight.compose.view.FeedMode
import streetlight.model.data.EntityCursor
import streetlight.model.data.EntityFeed
import streetlight.model.data.EventTag
import streetlight.model.data.FeedRequest
import streetlight.model.data.FeedType
import streetlight.model.io.ApiClient
import kotlin.time.Duration.Companion.milliseconds

/**
 * A feed shown in a view, starting from [page], with its type, the [modeState] it is laid out in, and the tag and search
 * that filter it. The feed is read again in [scope] when the type, the tag or the search changes, the search once it
 * rests. A view model holds one per feed it shows.
 */
class FeedModel(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    val modeState: MutableTap<FeedMode>,
    page: EntityFeed,
) {
    private val store = storeOf(FeedState(feed = page, type = page.source?.type ?: FeedType.Events))

    val feedState = store.tapOf { it.feed }
    val typeState = store.mutableTapOf({ it.type }) { copy(type = it) }
    val tagState = store.mutableTapOf({ it.tag }) { copy(tag = it) }
    val searchState = store.mutableTapOf({ it.search }) { copy(search = it) }

    init {
        scope.launch { typeState.flow.drop(1).collectLatest { read() } }
        scope.launch { tagState.flow.drop(1).collectLatest { read() } }
        scope.launch {
            searchState.flow.drop(1).collectLatest {
                delay(SearchRest)
                read()
            }
        }
    }

    /**
     * Reads the first page of the feed of the type chosen, filtered by the tag and search; a newer read cancels one in
     * flight.
     */
    private fun read() {
        readJob?.cancel()
        readJob = scope.launch {
            val state = store.now
            val source = state.feed.source?.copy(type = state.type) ?: return@launch
            val search = state.search.trim().takeIf { it.isNotEmpty() }
            val cursor = state.type.firstCursor.copy(tag = state.tag?.ordinal, search = search)
            // a failed read keeps the feed shown, as the view has no toaster yet
            val outcome = api.feed.readFeed(FeedRequest(source, cursor))
            if (outcome is Ok) store.set { copy(feed = outcome.data) }
        }
    }

    private var readJob: Job? = null
}

/** The state of a [FeedModel]. */
data class FeedState(
    val feed: EntityFeed,
    val type: FeedType,
    val tag: EventTag? = null,
    val search: String = "",
)

/** The cursor of the first page of a feed of this type: upcoming for events, newest first otherwise. */
private val FeedType.firstCursor get() = if (this == FeedType.Events) EntityCursor.Upcoming else EntityCursor.Default

/** The rest the search takes before the feed is read again, the web's debounce. */
private val SearchRest = 500.milliseconds
