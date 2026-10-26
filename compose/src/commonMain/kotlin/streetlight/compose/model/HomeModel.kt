package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.io.ApiClient

/** The content of [HomeRoute], read when the model is built: the home [feed] and the top galaxies. */
class HomeModel(private val api: ApiClient, settings: AppSettings) : ViewModel() {
    private val store = storeOf(HomeState())

    val feed = FeedModel(viewModelScope, api, settings.feedModeState)
    val galaxiesState = store.tapOf { it.galaxies }
    val isLoadingState = store.tapOf { it.isLoading }
    val problemState = store.tapOf { it.problem }

    init {
        viewModelScope.launch {
            when (val outcome = api.content.readHomeContent(null)) {
                is Ok -> {
                    feed.show(outcome.data.feed)
                    store.set { copy(galaxies = outcome.data.galaxies, isLoading = false) }
                }
                is Problem -> store.set { copy(problem = outcome.message, isLoading = false) }
            }
        }
    }
}

/** The state of a [HomeModel]. */
data class HomeState(
    val galaxies: List<Galaxy> = emptyList(),
    val isLoading: Boolean = true,
    val problem: String? = null,
)
