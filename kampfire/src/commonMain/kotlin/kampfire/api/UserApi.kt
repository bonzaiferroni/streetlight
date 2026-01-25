package kampfire.api

import kampfire.model.Auth
import kampfire.model.LoginRequest
import kampfire.model.PrivateInfo
import kampfire.model.SignUpRequest
import kampfire.model.SignUpResult
import kampfire.model.User

object UserApi : ApiNode(ApiNode(ApiNode(null, "api"),
    "v1"
), "user") {
    object Login : PostEndpoint<LoginRequest, Auth>(this, "login")
    object Create : PostEndpoint<SignUpRequest, SignUpResult>(this, "create")
    object UserInfo : GetEndpoint<PrivateInfo>(this, "private")
    object ReadInfo : GetEndpoint<User>(this)
    // object Update : PutEndpoint(this)
}

// "api/v1/user"