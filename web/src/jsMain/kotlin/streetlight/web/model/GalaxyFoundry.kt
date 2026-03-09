package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.GalaxyEdit
import streetlight.web.ui.ViewModel

class GalaxyFoundry(
    override val app: AppContext,
    val scope: CoroutineScope
): ViewModel {
    private val state = storeOf(GalaxyFoundryState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val galaxyFlow = stateFlow.mapDistinct { it.galaxy }
    val galaxyNow = state.now.galaxy

    fun setName(value: String) {
        setGalaxy { it.copy(name = value) }
    }

    fun setBlobUrl(value: String?) {
        state.set { it.copy(blobUrl = value) }
    }

    fun foundGalaxy() {
        val galaxy = state.now.galaxy.copy(center = geo.stateNow.center).takeIf { it.isValid } ?: return
        scope.launch {
            app.client.api.foundGalaxy(galaxy)
        }
    }

    private fun setGalaxy(block: (GalaxyEdit) -> GalaxyEdit) {
        state.set { it.copy(galaxy = block(galaxyNow)) }
    }
}

data class GalaxyFoundryState(
    val galaxy: GalaxyEdit = GalaxyEdit(),
    val blobUrl: String? = null
)