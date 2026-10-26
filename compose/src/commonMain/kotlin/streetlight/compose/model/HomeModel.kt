package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import streetlight.model.data.EntityCursor
import streetlight.model.data.EventTag
import streetlight.model.data.FeedRequest
import kotlin.time.Duration.Companion.milliseconds
import streetlight.model.data.EntityFeed
import streetlight.model.data.FeedType
import streetlight.model.data.Galaxy
import streetlight.model.io.ApiClient

/**
 * The home feed and top galaxies shown on [HomeRoute], read when the model is built, the type of feed chosen, and the
 * tag and search that filter the feed, which read it again when they change, the search once it rests.
 */
class HomeModel(private val api: ApiClient) : ViewModel() {
    private val store = storeOf(HomeState())

    val feedState = store.tapOf { it.feed }
    val galaxiesState = store.tapOf { it.galaxies }
    val feedTypeState = store.mutableTapOf({ it.feedType }) { copy(feedType = it) }
    val tagState = store.mutableTapOf({ it.tag }) { copy(tag = it) }
    val searchState = store.mutableTapOf({ it.search }) { copy(search = it) }
    val isLoadingState = store.tapOf { it.isLoading }
    val problemState = store.tapOf { it.problem }

    init {
        viewModelScope.launch {
            when (val outcome = api.content.readHomeContent(null)) {
                is Ok -> {
                    val feed = outcome.data.feed
                    val galaxies = outcome.data.galaxies
                    store.set { copy(feed = feed, galaxies = galaxies, feedType = feed.source?.type ?: feedType, isLoading = false) }
                }
                is Problem -> store.set { copy(problem = outcome.message, isLoading = false) }
            }
        }
        viewModelScope.launch { tagState.flow.drop(1).collectLatest { readFeed() } }
        viewModelScope.launch {
            searchState.flow.drop(1).collectLatest {
                delay(SearchRest)
                readFeed()
            }
        }
    }

    /** Reads the first page of the feed again, filtered by the tag and search; a newer read cancels one in flight. */
    private suspend fun readFeed() = coroutineScope {
        readJob?.cancel()
        readJob = launch {
            val source = store.now.feed?.source ?: return@launch
            val search = store.now.search.trim().takeIf { it.isNotEmpty() }
            val cursor = EntityCursor.Upcoming.copy(tag = store.now.tag?.ordinal, search = search)
            // a failed read keeps the feed shown, since the problem state would replace the whole view
            val outcome = api.feed.readFeed(FeedRequest(source, cursor))
            if (outcome is Ok) store.set { copy(feed = outcome.data) }
        }
    }

    private var readJob: Job? = null
}

/** How long the search rests before the feed is read again, the web's debounce. */
private val SearchRest = 500.milliseconds

/** The state of a [HomeModel]. */
data class HomeState(
    val feed: EntityFeed? = null,
    val galaxies: List<Galaxy> = emptyList(),
    val feedType: FeedType = FeedType.Events,
    val tag: EventTag? = null,
    val search: String = "",
    val isLoading: Boolean = true,
    val problem: String? = null,
)
