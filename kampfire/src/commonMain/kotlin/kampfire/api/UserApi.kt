package kampfire.api

import kampfire.model.Auth
import kampfire.model.LoginRequest
import kampfire.model.PrivateInfo
import kampfire.model.SignUpRequest
import kampfire.model.SignUpResult
import kampfire.model.BasicUserInfo

object UserApi: ApiNode(ApiNode(ApiNode(null, "api"),
    "v1"
), "user") {
    object Login: PostEndpoint<LoginRequest, Unit>(this, "login")
    object Refresh: PostEndpoint<Unit, Unit>(this, "refresh")
    object Logout: PostEndpoint<Unit, Unit>(this, "logout")
    object Create: PostEndpoint<SignUpRequest, SignUpResult>(this, "create")
    object Private: GetEndpoint<PrivateInfo>(this, "private")
    object CheckUsername: PostEndpoint<String, Boolean>(this, "check-username")
}

