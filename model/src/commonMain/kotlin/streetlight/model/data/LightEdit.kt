package streetlight.model.data

import kampfire.api.StringId
import kotlinx.serialization.Serializable

@Serializable
sealed interface EditLightRequest

@Serializable
data class LightEdit(
    val stringId: StringId,
    val isLit: Boolean,
    val lightType: LightType,
): EditLightRequest {
    fun getEventId(): EventId = stringId.toProjectId()
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