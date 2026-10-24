package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.launch
import streetlight.model.data.EntityFeed
import streetlight.model.data.FeedType
import streetlight.model.io.ApiClient

/** The home feed shown on [HomeRoute], read when the model is built, and the type of feed chosen. */
class HomeModel(private val api: ApiClient) : ViewModel() {
    private val store = storeOf(HomeState())

    val feedState = store.tapOf { it.feed }
    val feedTypeState = store.mutableTapOf({ it.feedType }) { copy(feedType = it) }
    val isLoadingState = store.tapOf { it.isLoading }
    val problemState = store.tapOf { it.problem }

    init {
        viewModelScope.launch {
            when (val outcome = api.content.readHomeContent(null)) {
                is Ok -> {
                    val feed = outcome.data.feed
                    store.set { copy(feed = feed, feedType = feed.source?.type ?: feedType, isLoading = false) }
                }
                is Problem -> store.set { copy(problem = outcome.message, isLoading = false) }
            }
        }
    }
}

/** The state of a [HomeModel]. */
data class HomeState(
    val feed: EntityFeed? = null,
    val feedType: FeedType = FeedType.Events,
    val isLoading: Boolean = true,
    val problem: String? = null,
)
