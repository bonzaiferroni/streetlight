package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface EditLightRequest

@Serializable
data class LightEdit(
    val targetId: Uuid,
    val isLit: Boolean,
    val starType: StarType,
): EditLightRequest {
    fun getEventId(): EventId = targetId.toRecordId()
}

@Serializable
data class MultiLightEdit(
    val edits: List<LightEdit>
): EditLightRequest

enum class StarType {
    Event,
    Galaxy,
    Location,
    Post,
}