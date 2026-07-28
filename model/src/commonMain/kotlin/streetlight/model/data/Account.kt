package streetlight.model.data

import kampfire.api.Email
import kampfire.model.AccountType
import koala.model.RouteContent
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val starId: StarId,
    val email: Email?,
    val accountType: AccountType,
    val emailStatus: EmailStatus?,
): RouteContent

enum class IdentityVisibility {
    Private,
    Contacts,
    Public
}

enum class EmailStatus {
    Unverified,
    Verified,
    Bounced,
    NotOwned,
}

// enum class AuthResult {
//     Confirmed,
//     AlreadyConsumed,
//     Expired,
//     NotFound,
//     ExpiredSource,
//     InternalError,
// }
