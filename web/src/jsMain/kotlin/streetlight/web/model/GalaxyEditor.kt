@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.api.toMarkdown
import kampfire.api.toSlug
import kampfire.model.Url
import kampfire.model.toDataOr
import koala.dom.MessageStore
import koala.model.GeoCamera
import koala.model.Portal
import kampfire.model.tapOf
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.City
import streetlight.model.data.DefaultLayout
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.Lean
import streetlight.model.data.GalaxyMark
import streetlight.model.data.MarkId
import streetlight.model.data.slugOf
import streetlight.model.ui.GalaxyRoute
import streetlight.web.io.ApiClient
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

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

    val designer = DesignEditor(api, DefaultLayout.galaxy, galaxy.design)

    val editState = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val imageField = editState.mutableTapOf({ it.image }) { copy(image = it) }
    val isLocalField = state.mutableTapOf({ it.isLocal }) { copy(isLocal = it) }
    val countryField = state.mutableTapOf({ it.country }) { copy(country = it) }
    val validityField = editState.tapOf { it.validity }
    val citiesField = state.tapOf { it.cities }
    val cityField = state.mutableTapOf({ it.city }) { copy(city = it) }
    val descriptionField = editState.mutableTapOf({ it.description ?: "".toMarkdown() }) { copy(description = it) }
    val taglineField = editState.mutableTapOf({ it.tagline ?: "" }) { copy(tagline = it) }
    val postGuideField = editState.mutableTapOf({ it.postGuide ?: "".toMarkdown() }) { copy(postGuide = it) }
    val reviewCountField = editState.mutableTapOf({ it.reviewCount?.toString() ?: "" }) { copy(reviewCount = it.toIntOrNull()) }
    val permissionField = editState.mutableTapOf({ it.postPermission }) { copy(postPermission = it) }
    val marksState = editState.mutableTapOf({ it.marks }) { copy(marks = it) }
    val markIdsState = editState.tapOf { it.marks.map { mark -> mark.markId } }

    val nameField = editState.mutableTapOf({ it.name ?: "" }) { value ->
        if (value.isNotEmpty() && !GalaxyEdit.isValidName(value)) return@mutableTapOf this
        val slug = slugOf(value)
        copy(name = value, slug = slug)
    }

    val slugField = editState.mutableTapOf({ it.slug?.value ?: "" }) { value ->
        if (value.isNotEmpty() && !GalaxyEdit.isValidSlug(value.trim().toSlug())) return@mutableTapOf this
        copy(slug = value.toSlug())
    }

    val cityQueryField = state.mutableTapOf({ it.cityQuery }) { copy(cityQuery = it) }

    val editMessage = MessageStore()
    val imageEditor = ImageEditor(imageField, api)

    init {
        galaxy.geoBounds?.let {
            geo.panMap(it)
        }

        scope.launch {
            cityQueryField.flow.debounce(500.milliseconds).collect { query ->
                if (query == stateNow.city?.name) return@collect
                val localities = api.searchCity(query, stateNow.country).toDataOr(toaster) { return@collect }
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

    fun addMark(name: String): Boolean {
        val trimmedName = name.trim().takeIf { it.length in GalaxyMark.ValidLength } ?: return false
        editState.set { copy(marks = marks + GalaxyMark(MarkId(Uuid.random()), Lean.Neutral, trimmedName)) }
        return true
    }

    fun removeMark(markId: MarkId) {
        editState.set { copy(marks = marks.filterNot { it.markId == markId }) }
    }

    fun setLean(markId: MarkId, lean: Lean) {
        val mark = editState.now.marks.firstOrNull { it.markId == markId } ?: error("mark not found")
        editState.set { copy(marks = marks.map { if (it.markId == markId) mark.copy(lean = lean) else it })}
    }

    fun submit() {
        val message = editState.now.validity.message

        if (message != null) {
            editMessage.deliver(message)
            return
        }

        editMessage.set("Saving...", true)
        scope.launch {
            imageEditor.finalizeImage(editMessage)
            val design = designer.build(editMessage)
            val edit = editState.now.copy(geoBounds = geo.stateNow.bounds, design = design)
            val slug = when (edit.galaxyId) {
                null -> api.createGalaxy(edit)
                else -> api.updateGalaxy(edit)
            }.toDataOr(editMessage) { return@launch }
            portal.go(GalaxyRoute(slug))
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
