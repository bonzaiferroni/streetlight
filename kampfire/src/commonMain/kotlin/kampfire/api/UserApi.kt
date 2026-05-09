package kampfire.api

import kampfire.model.Auth
import kampfire.model.LoginRequest
import kampfire.model.PrivateInfo
import kampfire.model.SignUpRequest
import kampfire.model.SignUpResult
import kampfire.model.BasicUserInfo

object UserApi : ApiNode(ApiNode(ApiNode(null, "api"),
    "v1"
), "user") {
    object Login : PostEndpoint<LoginRequest, Unit>(this, "login")
    object Create : PostEndpoint<SignUpRequest, SignUpResult>(this, "create")
    object Private : GetEndpoint<PrivateInfo>(this, "private")
    // object ReadInfo : GetEndpoint<BasicUserInfo>(this)
    // object Update : PostEndpoint<BasicUserInfo, Boolean>(this, "update")
    object CheckUsername: PostEndpoint<String, Boolean>(this, "check-username")
//    object UploadAvatar: PostEndpoint<ByteArray, String>(this, "upload-avatar")
}

// "api/v1/user"