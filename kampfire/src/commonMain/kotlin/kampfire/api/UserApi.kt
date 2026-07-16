package kampfire.api

import kampfire.model.AccountUpgradeRequest
import kampfire.model.LoginRequest
import kampfire.model.PrivateInfo
import kampfire.model.SignUpRequest

object UserApi: ApiNode(ApiNode(ApiNode(null, "api"),
    "v1"
), "user") {
    object Login: PostEndpoint<LoginRequest, Boolean>(this) {
        object CheckGuest: GetEndpoint<Username?>(this)
    }
    object Refresh: PostEndpoint<Unit, Unit>(this)
    object Logout: PostEndpoint<Unit, Boolean>(this)
    object Create: PostEndpoint<SignUpRequest, Boolean>(this)
    object Private: GetEndpoint<PrivateInfo>(this)
    object CheckUsernameExists: PostEndpoint<Username, Boolean>(this)
    object GenerateUsername: GetEndpoint<Username>(this)
    object AccountUpgrade: PostEndpoint<AccountUpgradeRequest, Boolean>(this)
}

