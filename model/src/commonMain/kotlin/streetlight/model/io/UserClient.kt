package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.FilePart
import kampfire.api.TextPart
import kampfire.api.postForm
import kotlinx.serialization.json.Json
import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.api.getApi
import kampfire.api.postApi
import kampfire.model.AccountUpgradeRequest
import kampfire.model.EmailChange
import kampfire.model.GeoPoint
import kampfire.model.GeoRect
import kampfire.model.LoginRequest
import kampfire.model.Outcome
import kampfire.model.PasswordChange
import kampfire.model.PasswordVerification
import kampfire.model.SignUpRequest
import kampfire.model.Url
import koala.Image
import koala.model.DocId
import koala.model.DocTableItem
import kotlin.uuid.Uuid
import streetlight.model.Api
import streetlight.model.data.*

/** The calls of `UserApi` and `Api.Users`: signing in and out, signing up, and uploads. */
class UserClient(private val transport: ApiTransport) {
    suspend fun createUser(request: SignUpRequest): Outcome<Unit> = transport.postApi(UserApi.Create, request)
    suspend fun login(request: LoginRequest): Outcome<Unit> = transport.postApi(UserApi.Login, request)
    suspend fun checkGuest(): Outcome<Username?> = transport.getApi(UserApi.Login.CheckGuest)
    suspend fun upgradeAccount(request: AccountUpgradeRequest): Outcome<Boolean> = transport.postApi(UserApi.AccountUpgrade, request)
    suspend fun logout(): Outcome<Boolean> = transport.postApi(UserApi.Logout, Unit)
    suspend fun checkUsernameExists(username: Username): Outcome<Boolean> = transport.postApi(UserApi.CheckUsernameExists, username)
    suspend fun generateUsername(): Outcome<Username> = transport.getApi(UserApi.GenerateUsername)
    /** Uploads [bytes], the file of [image], to be stored for [target], returning the stored image. */
    suspend fun uploadImage(image: Image, bytes: ByteArray, target: ImageTarget): Outcome<Image> =
        transport.postForm(Api.Users.UploadImage, listOf(
            TextPart("metadata", Json.encodeToString(image), "application/json"),
            TextPart("target", target.name),
            FilePart("file", bytes, "upload"),
        ))
    suspend fun readSkills(): Outcome<List<Skill>> = transport.getApi(Api.Users.Skills)
    suspend fun editSkill(skill: SkillEdit): Outcome<Skill> = transport.postApi(Api.Users.EditSkill, skill)
}
