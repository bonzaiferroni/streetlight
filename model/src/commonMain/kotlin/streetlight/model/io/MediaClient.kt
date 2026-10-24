package streetlight.model.io

import kampfire.api.ApiTransport
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

/** The calls of `Api.Medias`. */
class MediaClient(private val transport: ApiTransport) {
    suspend fun readMedia(slug: Slug): Outcome<Media> = transport.getApi(Api.Medias.ReadMedia, slug)
    suspend fun createMedia(edit: MediaEdit): Outcome<Media> = transport.postApi(Api.Medias.CreateMedia, edit)
    suspend fun updateMedia(edit: MediaEdit): Outcome<Media> = transport.postApi(Api.Medias.UpdateMedia, edit)
    suspend fun readSkillIds(mediaId: MediaId): Outcome<List<SkillId>> = transport.getApi(Api.Medias.ReadSkillIds, mediaId)
}
