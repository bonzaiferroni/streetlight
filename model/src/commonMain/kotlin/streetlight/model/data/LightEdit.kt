package streetlight.model.data

import kampfire.api.StringId
import kotlinx.serialization.Serializable

@Serializable
sealed interface LightRequest

@Serializable
data class LightEdit(
    val stringId: StringId,
    val isLit: Boolean
): LightRequest {
    fun getEventId(): EventId = stringId.toProjectId()
}

@Serializable
data class MultiLightEdit(
    val edits: List<LightEdit>
): LightRequest