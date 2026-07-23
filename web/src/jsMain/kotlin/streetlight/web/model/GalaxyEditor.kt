@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.api.toMarkdown
import kampfire.api.toSlug
import kampfire.model.Url
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.GeoCamera
import koala.model.Portal
import koala.model.fieldOf
import koala.model.mutableFieldOf
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.City
import streetlight.model.data.GalaxyEdit
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
    // val galaxyFlow = stateFlow.dedup { it.edit }
    val editNow get() = state.now.edit

    val editField = state.mutableFieldOf({ it.edit }) { copy(edit = it) }
    val imageField = editField.mutableFieldOf({ it.image }) { copy(image = it) }
    val isLocalField = state.mutableFieldOf({ it.isLocal }) { copy(isLocal = it) }
    val countryField = state.mutableFieldOf({ it.country }) { copy(country = it) }
    val validityField = editField.fieldOf { it.validity }
    val citiesField = state.fieldOf { it.cities }
    val cityField = state.mutableFieldOf({ it.city }) { copy(city = it) }
    val descriptionField = editField.mutableFieldOf({ it.description ?: "".toMarkdown() }) { copy(description = it) }
    val taglineField = editField.mutableFieldOf({ it.tagline ?: "" }) { copy(tagline = it) }
    val postGuideField = editField.mutableFieldOf({ it.postGuide ?: "".toMarkdown() }) { copy(postGuide = it) }
    val reviewCountField = editField.mutableFieldOf({ it.reviewCount?.toString() ?: "" }) { copy(reviewCount = it.toIntOrNull()) }
    val permissionField = editField.mutableFieldOf({ it.postPermission }) { copy(postPermission = it) }

    val nameField = editField.mutableFieldOf({ it.name ?: "" }) { value ->
        if (value.isNotEmpty() && !GalaxyEdit.isValidName(value)) return@mutableFieldOf this
        val slug = slugOf(value)
        copy(name = value, slug = slug)
    }

    val slugField = editField.mutableFieldOf({ it.slug?.value ?: "" }) { value ->
        if (value.isNotEmpty() && !GalaxyEdit.isValidSlug(value.trim().toSlug())) return@mutableFieldOf this
        copy(slug = value.toSlug())
    }

    val cityQueryField = state.mutableFieldOf({ it.cityQuery }) { copy(cityQuery = it) }

    val editMessage = MessageStore()
    val imageEditor = ImageEditor(imageField, api)

    init {
        galaxy.geoBounds?.let {
            geo.panMap(it)
        }

        scope.launch {
            cityQueryField.flow.debounce(500.milliseconds).collect { query ->
                if (query == stateNow.city?.name) return@collect
                val localities = api.searchCity(query, stateNow.country).handleResponse(toaster) ?: return@collect
                state.set { copy(cities = localities) }
            }
        }
    }

    fun setCity(value: City?) {
        state.set { copy(
            city = value,
            cityQuery = value?.name ?: cityQuery,
            edit = edit.copy(cityId = value?.cityId),
        ) }
        value?.geoPoint?.let {
            geo.panMap(it)
        }
    }

    fun submit() {
        val message = editField.now.validity.message

        if (message != null) {
            editMessage.deliver(message)
            return
        }

        editMessage.set("Saving...", true)
        scope.launch {
            imageEditor.finalizeImage(editMessage)
            val edit = editField.now.copy(geoBounds = geo.stateNow.bounds)
            when (edit.galaxyId) {
                null -> api.createGalaxy(edit)
                else -> api.updateGalaxy(edit)
            }.handleResponse(editMessage) { slug ->
                portal.go(GalaxyRoute(slug))
            }
        }
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
