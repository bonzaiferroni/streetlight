package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class StarEdit(
    val stringId: String,
    val isStar: Boolean
)