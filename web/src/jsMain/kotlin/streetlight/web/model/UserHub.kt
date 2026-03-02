package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Talent
import streetlight.web.io.ApiClient

class UserHub(
    private val scope: CoroutineScope,
    private val api: ApiClient
) {
    private val view = storeOf(UserHubState())

    val talentsFlow = view.flow.mapDistinct { it.talents }

    init {
        refreshTalents()
    }

    fun refreshTalents() {
        scope.launch {
            val talents = api.readTalents() ?: emptyList()
            view.set { it.copy(talents = talents) }
        }
    }
}

data class UserHubState(
    val talents: List<Talent> = emptyList()
)