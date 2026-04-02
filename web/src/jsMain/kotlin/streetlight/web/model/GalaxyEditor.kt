package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.PostPermission
import streetlight.model.data.ReviewMode
import streetlight.web.GalaxyPathIdRoute
import streetlight.web.ui.ViewModel

class GalaxyEditor(
    override val app: Streetlight,
    val scope: CoroutineScope
): ViewModel {
    private val state = storeOf(GalaxyFoundryState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val galaxyFlow = stateFlow.mapDistinct { it.galaxy }
    val galaxyNow get() = state.now.galaxy

    fun setName(value: String) {
        if (!GalaxyEdit.isValidName(value)) return
        val path = GalaxyEdit.pathOf(value)
        setGalaxy { it.copy(name = value, path = path) }
    }

    fun setPath(value: String) {
        if (!GalaxyEdit.isValidPath(value)) return
        setGalaxy { it.copy(path = value) }
    }

    fun setBlobUrl(value: String?) = state.set { it.copy(blobUrl = value) }

    fun setDescription(value: String) = setGalaxy { it.copy(description = value) }

    fun setPostPermission(permission: PostPermission) = setGalaxy { it.copy(postPermission = permission) }

    fun setPostGuide(value: String) = setGalaxy { it.copy(postGuide = value) }

    fun setReviewMode(value: ReviewMode) = setGalaxy { it.copy(reviewMode = value) }

    fun foundGalaxy() {
        val geoState = geo.stateNow
        val galaxy = galaxyNow.copy(
            center = geoState.center,
            zoom = geoState.zoom
        ).takeIf { it.isValid } ?: return
        scope.launch {
            console.log(galaxy.description)
            val imageUrl = stateNow.blobUrl?.let {
                api.uploadImage(it)
            }
            val galaxy = app.client.api.foundGalaxy(galaxy.copy(imageUrl = imageUrl))
            if (galaxy != null) {
                portal.go(GalaxyPathIdRoute(galaxy.path))
                reset()
            }
        }
    }

    private fun reset() {
        state.set { GalaxyFoundryState() }
    }

    private fun setGalaxy(block: (GalaxyEdit) -> GalaxyEdit) {
        state.set { it.copy(galaxy = block(it.galaxy)) }
    }
}

data class GalaxyFoundryState(
    val galaxy: GalaxyEdit = GalaxyEdit(),
    val blobUrl: String? = null
)
