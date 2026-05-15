@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.Url
import koala.dom.UIMessage
import koala.dom.set
import koala.model.GeoMap
import koala.model.Portal
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.Locality
import streetlight.model.data.PostPermission
import streetlight.model.data.ReviewMode
import streetlight.model.data.slugOf
import streetlight.web.GalaxyRoute
import streetlight.web.io.ApiClient
import streetlight.web.io.getDataOrNull

class GalaxyEditor(
    galaxy: GalaxyEdit?,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val geo: GeoMap,
    private val portal: Portal,
) {
    private val state = storeOf(GalaxyFoundryState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val galaxyFlow = stateFlow.mapDistinct { it.galaxy }
    val galaxyNow get() = state.now.galaxy
    val msg = storeOf<UIMessage?>(UIMessage(galaxy?.invalidMessage ?: "Looks good."))

    init {
        scope.launch {
            stateFlow.mapDistinct { it.cityQuery }.debounce(500L).collect { query ->
                if (query == stateNow.locality?.city) return@collect
                val localities = api.searchCity(query, stateNow.country).getDataOrNull() ?: return@collect
                state.set { it.copy(localities = localities) }
            }
        }
    }

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

    fun setIsLocal(value: Boolean) = state.set { it.copy(isLocal = value) }

    fun setCityQuery(query: String) = state.set { it.copy(cityQuery = query)}

    fun setCountry(value: String) = state.set { it.copy(country = value) }

    fun setLocality(value: Locality?) = state.set { it.copy(
        locality = value,
        cityQuery = value?.city ?: it.cityQuery,
        galaxy = it.galaxy.copy(cityId = value?.cityId),
    ) }

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
            val galaxy = api.foundGalaxy(galaxy.copy(imageRef = imageUrl))
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
    val blobUrl: Url? = null,
    val isLocal: Boolean = true,
    val cityQuery: String = "",
    val localities: List<Locality> = emptyList(),
    val country: String = "United States",
    val locality: Locality? = null,
)
