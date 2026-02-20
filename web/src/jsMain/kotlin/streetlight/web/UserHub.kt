package streetlight.web

import koala.model.mapDistinct
import koala.model.stateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.NewSong
import streetlight.model.data.Talent

class UserHub(
    private val scope: CoroutineScope,
    private val api: ApiClient
) {
    private val view = stateOf(UserHubState())

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