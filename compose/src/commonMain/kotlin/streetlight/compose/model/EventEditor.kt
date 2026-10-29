package streetlight.compose.model

import kampfire.api.toMarkdown
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.model.ExtraLink
import koala.model.MessageStore
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.ImageTarget
import streetlight.model.data.LocationId
import streetlight.model.io.ApiClient

/**
 * The edit of an event being created or updated, its fields, its [imageEditor], and its saving. A submodel, held by the
 * view model of any view that edits an event, as `EventScout` does. The cousin of the web's `EventEditor`.
 */
class EventEditor(private val api: ApiClient, initialEdit: EventEdit = newEventEdit()) {
    private val store = storeOf(EventEditorState(initialEdit))

    val editState = store.tapOf { it.edit }
    val titleState = editStateOf({ it.title.orEmpty() }) { copy(title = it.takeIf(String::isNotBlank)) }
    val isFreeState = store.mutableTapOf({ it.edit.isFree }) {
        copy(edit = edit.copy(cost = if (it) 0f else costText.toFloatOrNull()))
    }
    val costTextState = store.mutableTapOf({ it.costText }) {
        copy(costText = it, edit = edit.copy(cost = it.toFloatOrNull()))
    }
    val startTimeState = editStateOf<LocalTime?>({ it.startTime }) { copy(startTime = it) }
    val endTimeState = editStateOf<LocalTime?>({ it.endTime }) { copy(endTime = it) }
    val dateState = editStateOf<LocalDate?>({ it.date }) { copy(date = it) }
    val descriptionState = editStateOf({ it.description ?: "".toMarkdown() }) {
        copy(description = it.takeIf { markdown -> markdown.value.isNotBlank() })
    }
    val websiteState = editStateOf({ it.website?.value.orEmpty() }) { copy(website = it.toUrlOrNull()) }
    val validityState = store.tapOf { it.edit.validity }
    val linksState = editStateOf({ it.links.orEmpty() }) { copy(links = it.takeIf(List<ExtraLink>::isNotEmpty)) }
    val messages = MessageStore()
    val imageEditor = ImageEditor(ImageTarget.Event, api).apply { setImage(initialEdit.image) }
    val isSavingState = store.tapOf { it.isSaving }

    val editNow get() = store.now.edit

    /** Starts editing [edit], clearing the last message. */
    fun setEdit(edit: EventEdit) {
        store.set { EventEditorState(edit) }
        imageEditor.setImage(edit.image)
        messages.clear()
    }

    fun setLocationId(locationId: LocationId?) = store.set { copy(edit = edit.copy(locationId = locationId)) }

    /** Whether the edit is valid, showing the reason when it is not. */
    fun isEditValid(): Boolean {
        val message = editNow.validity.message
        messages.set(message)
        return message == null
    }

    /**
     * Saves the event, uploading a picked image first and creating the event when it is new, and returns it, or `null`
     * when it is invalid or the save fails.
     */
    suspend fun saveEvent(): Event? {
        if (!isEditValid()) return null
        store.set { copy(isSaving = true) }
        messages.deliverSending("Uploading image...")
        val image = imageEditor.finalizeImage().toDataOr(messages) {
            store.set { copy(isSaving = false) }
            return null
        }
        val edit = editNow.copy(image = image, website = editNow.website?.withScheme())
        messages.deliverSending("Saving...")
        val outcome = when (edit.eventId) {
            null -> api.event.createEvent(edit)
            else -> api.event.updateEvent(edit)
        }
        val event = outcome.toDataOr(messages) {
            store.set { copy(isSaving = false) }
            return null
        }
        store.set { copy(edit = edit.copy(eventId = event.eventId), isSaving = false) }
        messages.clear()
        return event
    }

    private fun <T> editStateOf(read: (EventEdit) -> T, write: EventEdit.(T) -> EventEdit) =
        store.mutableTapOf({ read(it.edit) }) { copy(edit = edit.write(it)) }
}

data class EventEditorState(
    val edit: EventEdit,
    val costText: String = edit.cost?.toString().orEmpty(),
    val isSaving: Boolean = false,
)

fun newEventEdit(title: String? = null, locationId: LocationId? = null) =
    EventEdit(title = title, locationId = locationId, timeZoneId = TimeZone.currentSystemDefault().id)
