package streetlight.compose.model

import kampfire.api.toMarkdown
import kampfire.model.Url
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import kampfire.model.toUrl
import streetlight.model.data.ImageTarget
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.io.ApiClient

/**
 * The edit of a location being created or updated, its fields, its [imageEditor], and its saving. A submodel, held by
 * the view model of any view that edits a location, as `LocationScout` does. The cousin of the web's `LocationEditor`.
 */
class LocationEditor(private val api: ApiClient, edit: LocationEdit = LocationEdit()) {
    private val store = storeOf(LocationEditorState(edit))

    val editState = store.tapOf { it.edit }
    val nameState = editStateOf({ it.name.orEmpty() }) { copy(name = it.takeIf(String::isNotBlank)) }
    val addressState = editStateOf({ it.address.orEmpty() }) { copy(address = it.takeIf(String::isNotBlank)) }
    val cityState = editStateOf({ it.city.orEmpty() }) { copy(city = it.takeIf(String::isNotBlank)) }
    val descriptionState = editStateOf({ it.description ?: "".toMarkdown() }) {
        copy(description = it.takeIf { markdown -> markdown.value.isNotBlank() })
    }
    val websiteState = editStateOf({ it.website?.value.orEmpty() }) { copy(website = it.toUrlOrNull()) }
    val eventsUrlState = editStateOf({ it.eventsUrl?.value.orEmpty() }) { copy(eventsUrl = it.toUrlOrNull()) }
    val isHostState = editStateOf({ it.isHost == true }) { copy(isHost = it) }
    val messageState = store.tapOf { it.message }
    val imageEditor = ImageEditor(ImageTarget.Location, api).apply { setImage(edit.image) }
    val isSavingState = store.tapOf { it.isSaving }

    val editNow get() = store.now.edit

    /** Starts editing [edit], clearing the last message. */
    fun setEdit(edit: LocationEdit) {
        store.set { LocationEditorState(edit) }
        imageEditor.setImage(edit.image)
    }

    /** Whether the edit is valid, showing the reason when it is not. */
    fun isEditValid(): Boolean {
        val message = editNow.validity.message
        store.set { copy(message = message) }
        return message == null
    }

    /**
     * Saves the location, uploading a picked image first and creating the location when it is new, and returns it, or
     * `null` when it is invalid or the save fails.
     */
    suspend fun saveLocation(): Location? {
        if (!isEditValid()) return null
        store.set { copy(message = "Uploading image...", isSaving = true) }
        val image = imageEditor.finalizeImage().toDataOr {
            store.set { copy(message = it.message, isSaving = false) }
            return null
        }
        val edit = editNow.copy(image = image)
        store.set { copy(message = "Saving...") }
        val outcome = when (edit.locationId) {
            null -> api.location.createLocation(edit)
            else -> api.location.updateLocation(edit)
        }
        val location = outcome.toDataOr {
            store.set { copy(message = it.message, isSaving = false) }
            return null
        }
        store.set { copy(edit = edit.copy(locationId = location.locationId), message = null, isSaving = false) }
        return location
    }

    private fun <T> editStateOf(read: (LocationEdit) -> T, write: LocationEdit.(T) -> LocationEdit) =
        store.mutableTapOf({ read(it.edit) }) { copy(edit = edit.write(it)) }
}

/** The [edit] of a [LocationEditor], its last [message], and whether it [isSaving]. */
data class LocationEditorState(
    val edit: LocationEdit,
    val message: String? = null,
    val isSaving: Boolean = false,
)

/** This text as a url, or `null` when blank. */
private fun String.toUrlOrNull(): Url? = trim().takeIf { it.isNotEmpty() }?.toUrl()
