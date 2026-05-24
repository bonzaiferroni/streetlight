package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface EditLightRequest

@Serializable
data class LightEdit(
    val targetId: Uuid,
    val isLit: Boolean,
    val lightType: LightType,
): EditLightRequest {
    fun getEventId(): EventId = targetId.toRecordId()
}

@Serializable
data class MultiLightEdit(
    val edits: List<LightEdit>
): EditLightRequest

enum class LightType {
    Event,
    Galaxy,
    Location,
}