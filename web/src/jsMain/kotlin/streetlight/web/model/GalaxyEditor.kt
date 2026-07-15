@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.api.Markdown
import kampfire.api.toSlug
import kampfire.model.Url
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.GeoCamera
import koala.model.Portal
import koala.model.tap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.City
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.PostPermission
import streetlight.model.data.slugOf
import streetlight.model.ui.GalaxyRoute
import streetlight.web.io.ApiClient
import kotlin.time.Duration.Companion.milliseconds

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
    val galaxyFlow = stateFlow.tap { it.edit }
    val editNow get() = state.now.edit

    val editMessage = MessageStore()
    val imageEditor = ImageEditor(galaxy.image, api)

    val validityFlow = stateFlow.tap { it.edit.validity }

    init {
        galaxy.geoBounds?.let {
            geo.panMap(it)
        }
        scope.launch {
            stateFlow.tap { it.cityQuery }.debounce(500.milliseconds).collect { query ->
                if (query == stateNow.city?.name) return@collect
                val localities = api.searchCity(query, stateNow.country).handleResponse(toaster) ?: return@collect
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

    fun setDescription(value: Markdown) = setGalaxy { it.copy(description = value) }

    fun setPostPermission(permission: PostPermission) = setGalaxy { it.copy(postPermission = permission) }

    fun setPostGuide(value: Markdown) = setGalaxy { it.copy(postGuide = value) }

    fun setReviewCount(value: Int?) = setGalaxy { it.copy(reviewCount = value) }

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
        val message = editNow.validity.message

        if (message != null) {
            editMessage.receive(message)
            return
        }

        editMessage.set("Saving...", true)
        scope.launch {
            val image = imageEditor.finalizeImage(editMessage)
            val edit = editNow.copy(geoBounds = geo.stateNow.bounds, image = image)
            when (edit.galaxyId) {
                null -> api.createGalaxy(edit)
                else -> api.updateGalaxy(edit)
            }.handleResponse(editMessage) { slug ->
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
    val imageUrl: Url? = edit.image?.url,
    val isLocal: Boolean = true,
    val cityQuery: String = "",
    val cities: List<City> = emptyList(),
    val country: String = "United States",
    val city: City? = null,
)
