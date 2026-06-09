@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.api.toSlug
import kampfire.model.Url
import kampfire.model.getDataOrNull
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.GeoCamera
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
    galaxy: GalaxyEdit,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val geo: GeoCamera,
    private val portal: Portal,
    private val toaster: Toaster,
) {
    private val state = storeOf(GalaxyFoundryState(galaxy))
    val stateFlow = state.flow
    val stateNow get() = state.now
    val galaxyFlow = stateFlow.mapDistinct { it.edit }
    val editNow get() = state.now.edit

    val editMessage = MessageStore()

    val validityFlow = stateFlow.mapDistinct { it.edit.validity }

    init {
        galaxy.geoBounds?.let {
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

    fun setSlug(value: String) {
        if (value.isNotEmpty() && !GalaxyEdit.isValidSlug(value.trim().toSlug())) return
        setGalaxy { it.copy(slug = value.toSlug()) }
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
            edit = it.edit.copy(cityId = value?.cityId),
        ) }
        value?.geoPoint?.let {
            geo.panMap(it)
        }
    }

    fun submit() {
        val edit = editNow.copy(geoBounds = geo.stateNow.bounds)
        val message = edit.validity.message
        val imageRef = stateNow.edit.imageRef

        if (message != null) {
            editMessage.set(message)
            return
        }

        editMessage.set("Saving...", true)
        scope.launch {
            val imageUrl: Url? = stateNow.imageUrl.takeIf { it != imageRef }?.let { url ->
                api.uploadImage(url).getDataOrNull() ?: return@launch
            } ?: imageRef
            api.createOrUpdateGalaxy(edit.copy(imageRef = imageUrl)).handleResponse(editMessage::set) { slug ->
                portal.go(GalaxyRoute(slug))
            }
        }
    }

    private fun setGalaxy(block: (GalaxyEdit) -> GalaxyEdit) {
        val edit = block(stateNow.edit)
        state.set { it.copy(edit = edit) }
    }
}

data class GalaxyFoundryState(
    val edit: GalaxyEdit,
    val imageUrl: Url? = edit.imageRef,
    val isLocal: Boolean = true,
    val cityQuery: String = "",
    val cities: List<City> = emptyList(),
    val country: String = "United States",
    val city: City? = null,
)
