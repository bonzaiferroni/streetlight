package kampfire.api

import kampfire.model.Auth
import kampfire.model.LoginRequest
import kampfire.model.PrivateInfo
import kampfire.model.SignUpRequest
import kampfire.model.SignUpResult
import kampfire.model.User
import kampfire.model.UserInfo

object UserApi : ApiNode(ApiNode(ApiNode(null, "api"),
    "v1"
), "user") {
    object Login : PostEndpoint<LoginRequest, Auth>(this, "login")
    object Create : PostEndpoint<SignUpRequest, SignUpResult>(this, "create")
    object Private : GetEndpoint<PrivateInfo>(this, "private")
    object ReadInfo : GetEndpoint<UserInfo>(this)
    // object Update : PutEndpoint(this)
}

// "api/v1/user"