package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/** A request to light or unlight records. */
@Serializable
sealed interface EditLightRequest

/** A request to light or unlight one record, the way a user stars it. */
@Serializable
data class LightEdit(
    val targetId: Uuid,
    val isLit: Boolean,
    val toggleType: ToggleType,
): EditLightRequest {
    fun getEventId(): EventId = targetId.toRecordId()
}

/** A request to light or unlight several records together. */
@Serializable
data class MultiLightEdit(
    val edits: List<LightEdit>
): EditLightRequest

/** The kinds of record a user can light. */
enum class ToggleType {
    Event,
    Galaxy,
    Location,
    Post,
}