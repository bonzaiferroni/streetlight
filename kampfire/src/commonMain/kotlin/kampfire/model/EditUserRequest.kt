package kampfire.model

import kotlinx.serialization.Serializable

/** A request to change or remove the details of an account. */
@Serializable
data class EditUserRequest(
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val deleteEmail: Boolean = false,
    val deleteName: Boolean = false,
    val deleteUser: Boolean = false,
) {
}