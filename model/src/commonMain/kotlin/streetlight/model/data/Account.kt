package streetlight.model.data

import kampfire.api.EmailAddress
import kampfire.model.AccountType
import koala.model.RouteContent
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val starId: StarId,
    val email: EmailAddress?,
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

val Account.viableEmail get() = when (emailStatus) {
    EmailStatus.Bounced, EmailStatus.NotOwned -> null
    else -> email
}

// enum class AuthResult {
//     Confirmed,
//     AlreadyConsumed,
//     Expired,
//     NotFound,
//     ExpiredSource,
//     InternalError,
// }
