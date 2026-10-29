package streetlight.compose.model

import kampfire.api.toMarkdown
import kampfire.model.Url
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.toAbsoluteUrlOrNull
import kampfire.model.tapOf
import kampfire.model.toDataOr
import kampfire.model.toUrl
import koala.model.MessageStore
import streetlight.model.data.ImageTarget
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.io.ApiClient

/**
 * The edit of a location being created or updated, its fields, its [imageEditor], and its saving. A submodel, held by
 * the view model of any view that edits a location, as `LocationScout` does. The cousin of the web's `LocationEditor`.
 */
class LocationEditor(private val api: ApiClient, initialEdit: LocationEdit = LocationEdit()) {
    private val store = storeOf(LocationEditorState(initialEdit))

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
    val messages = MessageStore()
    val imageEditor = ImageEditor(ImageTarget.Location, api).apply { setImage(initialEdit.image) }
    val isSavingState = store.tapOf { it.isSaving }

    val editNow get() = store.now.edit

    /** Starts editing [edit], clearing the last message. */
    fun setEdit(edit: LocationEdit) {
        store.set { LocationEditorState(edit) }
        messages.clear()
        imageEditor.setImage(edit.image)
    }

    /** Whether the edit is valid, showing the reason when it is not. */
    fun isEditValid(): Boolean {
        val message = editNow.validity.message
        messages.set(message)
        return message == null
    }

    /**
     * Saves the location, uploading a picked image first and creating the location when it is new, and returns it, or
     * `null` when it is invalid or the save fails.
     */
    suspend fun saveLocation(): Location? {
        if (!isEditValid()) return null
        store.set { copy(isSaving = true) }
        messages.deliverSending("Uploading image...")
        val image = imageEditor.finalizeImage().toDataOr(messages) {
            store.set { copy(isSaving = false) }
            return null
        }
        val edit = editNow.copy(
            image = image,
            website = editNow.website?.withScheme(),
            eventsUrl = editNow.eventsUrl?.withScheme(),
        )
        messages.deliverSending("Saving...")
        val outcome = when (edit.locationId) {
            null -> api.location.createLocation(edit)
            else -> api.location.updateLocation(edit)
        }
        val location = outcome.toDataOr(messages) {
            store.set { copy(isSaving = false) }
            return null
        }
        store.set { copy(edit = edit.copy(locationId = location.locationId), isSaving = false) }
        messages.clear()
        return location
    }

    private fun <T> editStateOf(read: (LocationEdit) -> T, write: LocationEdit.(T) -> LocationEdit) =
        store.mutableTapOf({ read(it.edit) }) { copy(edit = edit.write(it)) }
}

data class LocationEditorState(
    val edit: LocationEdit,
    val isSaving: Boolean = false,
)

/** This text as a url, or `null` when blank. */
internal fun String.toUrlOrNull(): Url? = trim().takeIf { it.isNotEmpty() }?.toUrl()

/** This url with `https://` in front when it was typed without a scheme, or as typed when it cannot take one. */
internal fun Url.withScheme() = value.toAbsoluteUrlOrNull() ?: this
