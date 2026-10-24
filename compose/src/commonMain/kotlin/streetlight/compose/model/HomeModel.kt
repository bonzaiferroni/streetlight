package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.launch
import streetlight.model.data.Entity
import streetlight.model.io.ApiClient

/** The home feed shown on [HomeRoute], read when the model is built. */
class HomeModel(private val api: ApiClient) : ViewModel() {
    private val store = storeOf(HomeState())

    val entitiesState = store.tapOf { it.entities }
    val isLoadingState = store.tapOf { it.isLoading }
    val problemState = store.tapOf { it.problem }

    init {
        viewModelScope.launch {
            when (val outcome = api.content.readHomeContent(null)) {
                is Ok -> store.set { copy(entities = outcome.data.feed.entities, isLoading = false) }
                is Problem -> store.set { copy(problem = outcome.message, isLoading = false) }
            }
        }
    }
}

/** The state of a [HomeModel]. */
data class HomeState(
    val entities: List<Entity> = emptyList(),
    val isLoading: Boolean = true,
    val problem: String? = null,
)
