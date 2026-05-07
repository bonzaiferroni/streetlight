package streetlight.web.model

import kampfire.model.Url
import koala.dom.UIMessage
import koala.dom.set
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.PostPermission
import streetlight.model.data.ReviewMode
import streetlight.model.data.slugOf
import streetlight.web.GalaxyRoute
import streetlight.web.ui.ViewModel

class GalaxyEditor(
    galaxy: GalaxyEdit?,
    override val app: Streetlight,
    val scope: CoroutineScope
): ViewModel {
    private val state = storeOf(GalaxyFoundryState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val galaxyFlow = stateFlow.mapDistinct { it.galaxy }
    val galaxyNow get() = state.now.galaxy
    val msg = storeOf(UIMessage(galaxy?.invalidMessage ?: "Looks good."))

    fun setName(value: String) {
        if (!GalaxyEdit.isValidName(value)) return
        val path = slugOf(value)
        setGalaxy { it.copy(name = value, slug = path) }
    }

    fun setPath(value: String) {
        if (!GalaxyEdit.isValidPath(value)) return
        setGalaxy { it.copy(slug = value) }
    }

    fun setBlobUrl(value: Url?) = state.set { it.copy(blobUrl = value) }

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
        msg.set("Founding ${galaxy.name}...")
        scope.launch {
            val imageUrl = stateNow.blobUrl?.let {
                api.uploadImage(it) ?: error("failed to upload image")
            }
            val galaxy = app.client.api.foundGalaxy(galaxy.copy(imageRef = imageUrl))
            if (galaxy != null) {
                portal.go(GalaxyRoute(galaxy.slug))
                reset()
            }
        }
    }

    private fun reset() {
        state.set { GalaxyFoundryState() }
    }

    private fun setGalaxy(block: (GalaxyEdit) -> GalaxyEdit) {
        val edit = block(stateNow.galaxy)
        state.set { it.copy(galaxy = edit) }
        msg.set(edit.invalidMessage ?: "Looks good.")
    }
}

data class GalaxyFoundryState(
    val galaxy: GalaxyEdit = GalaxyEdit(),
    val blobUrl: Url? = null
)
