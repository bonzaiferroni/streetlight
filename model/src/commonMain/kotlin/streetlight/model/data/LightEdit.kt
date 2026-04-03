package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class LightEdit(
    val stringId: String,
    val isLit: Boolean
)