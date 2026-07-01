package kampfire.api

import kampfire.model.LoginRequest
import kampfire.model.PrivateInfo
import kampfire.model.SignUpRequest

object UserApi: ApiNode(ApiNode(ApiNode(null, "api"),
    "v1"
), "user") {
    object Login: PostEndpoint<LoginRequest, Boolean>(this, "login")
    object Refresh: PostEndpoint<Unit, Unit>(this, "refresh")
    object Logout: PostEndpoint<Unit, Boolean>(this, "logout")
    object Create: PostEndpoint<SignUpRequest, Boolean>(this, "create")
    object Private: GetEndpoint<PrivateInfo>(this, "private")
    object CheckUsername: PostEndpoint<Username, Boolean>(this, "check-username")
    object GenerateUsername: GetEndpoint<Username>(this, "generate-username")
}

