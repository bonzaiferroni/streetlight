@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.Url
import kampfire.model.getDataOrNull
import kampfire.model.handleResponse
import koala.model.GeoMap
import koala.model.Portal
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.City
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.PostPermission
import streetlight.model.data.ReviewMode
import streetlight.model.data.slugOf
import streetlight.web.GalaxyRoute
import streetlight.web.io.ApiClient

class GalaxyEditor(
    galaxy: GalaxyEdit?,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val geo: GeoMap,
    private val portal: Portal,
    private val toaster: Toaster,
) {
    private val state = storeOf(GalaxyFoundryState(galaxy ?: GalaxyEdit()))
    val stateFlow = state.flow
    val stateNow get() = state.now
    val galaxyFlow = stateFlow.mapDistinct { it.galaxy }
    val editNow get() = state.now.galaxy

    init {
        galaxy?.geoBounds?.let {
            geo.panMap(it)
        }
        scope.launch {
            stateFlow.mapDistinct { it.cityQuery }.debounce(500L).collect { query ->
                if (query == stateNow.city?.name) return@collect
                val localities = api.searchCity(query, stateNow.country).handleResponse(toaster::toast) ?: return@collect
                state.set { it.copy(cities = localities) }
            }
        }
    }

    fun setName(value: String) {
        if (value.isNotEmpty() && !GalaxyEdit.isValidName(value)) return
        val path = slugOf(value)
        setGalaxy { it.copy(name = value, slug = path) }
    }

    fun setPath(value: String) {
        if (value.isNotEmpty() && !GalaxyEdit.isValidPath(value)) return
        setGalaxy { it.copy(slug = value) }
    }

    fun setImageUrl(value: Url?) = state.set { it.copy(imageUrl = value) }

    fun setDescription(value: String) = setGalaxy { it.copy(description = value) }

    fun setPostPermission(permission: PostPermission) = setGalaxy { it.copy(postPermission = permission) }

    fun setPostGuide(value: String) = setGalaxy { it.copy(postGuide = value) }

    fun setReviewMode(value: ReviewMode) = setGalaxy { it.copy(reviewMode = value) }

    fun setIsLocal(value: Boolean) = state.set { it.copy(isLocal = value) }

    fun setCityQuery(query: String) = state.set { it.copy(cityQuery = query)}

    fun setCountry(value: String) = state.set { it.copy(country = value) }

    fun setTagline(value: String) = setGalaxy { it.copy(tagline = value) }

    fun setCity(value: City?) {
        state.set { it.copy(
            city = value,
            cityQuery = value?.name ?: it.cityQuery,
            galaxy = it.galaxy.copy(cityId = value?.cityId),
        ) }
        value?.geoPoint?.let {
            geo.panMap(it)
        }
    }

    fun submit() {
        val geoState = geo.stateNow
        val edit = editNow.copy(
            geoBounds = geoState.bounds
        )
        val invalidParts = edit.invalidParts
        if (invalidParts.isNotEmpty()) {
            toaster.toast("Missing: ${invalidParts.joinToString(", ")}")
            return
        }

        toaster.toast("Saving...")
        val imageRef = stateNow.galaxy.imageRef
        scope.launch {
            val imageUrl: Url? = stateNow.imageUrl.takeIf { it != imageRef }?.let { url ->
                api.uploadImage(url).getDataOrNull() ?: return@launch
            } ?: imageRef
            api.createOrUpdateGalaxy(edit.copy(imageRef = imageUrl)).handleResponse(toaster::toast) { galaxy ->
                portal.go(GalaxyRoute(galaxy.slug))
            }
        }
    }

    private fun setGalaxy(block: (GalaxyEdit) -> GalaxyEdit) {
        val edit = block(stateNow.galaxy)
        state.set { it.copy(galaxy = edit) }
    }
}

data class GalaxyFoundryState(
    val galaxy: GalaxyEdit,
    val imageUrl: Url? = galaxy.imageRef,
    val isLocal: Boolean = true,
    val cityQuery: String = "",
    val cities: List<City> = emptyList(),
    val country: String = "United States",
    val city: City? = null,
)
