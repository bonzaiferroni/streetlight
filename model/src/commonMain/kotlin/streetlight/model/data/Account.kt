package streetlight.model.data

import kampfire.api.Email
import kampfire.model.AccountType
import koala.model.RouteContent
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val identityVisibility: IdentityVisibility,
    val name: String?,
    val email: Email?,
    val cityId: CityId?,
    val accountType: AccountType
): RouteContent

enum class IdentityVisibility {
    Private,
    Contacts,
    Public
}