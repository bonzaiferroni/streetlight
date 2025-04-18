package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class EditUserRequest(
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val venmo: String = "",
    val deleteEmail: Boolean = false,
    val deleteName: Boolean = false,
    val deleteUser: Boolean = false,
) {
}