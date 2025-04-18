package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String,
    val socialMedia: String,
)