package streetlight.model.data

import kampfire.api.EmailAddress
import kampfire.model.AccountType
import koala.model.FetcherContent
import koala.model.RouteContent
import kotlinx.serialization.Serializable

/** The private details of a user's account: its email and the state of its verification. */
@Serializable
data class Account(
    val starId: StarId,
    val email: EmailAddress?,
    val accountType: AccountType,
    val emailStatus: EmailStatus?,
): RouteContent

/** Who may see a user's identity. */
enum class IdentityVisibility {
    Private,
    Contacts,
    Public
}

/** The state of an account's email address: unverified, verified, bounced, or disowned by the person who received it. */
enum class EmailStatus {
    Unverified,
    Verified,
    Bounced,
    NotOwned,
}

/** The email address when mail can be sent to it, or `null`. */
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
