package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.openEvents
import kotlinx.coroutines.flow.Flow
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

/** The calls of `Api.Talk`, and the talk event stream. */
class TalkClient(private val transport: ApiTransport) {
    suspend fun readHistory(spaceId: Uuid, spaceType: SpaceType): Outcome<List<Comment>> =
        transport.getApi(Api.Talk.ReadHistory) {
            writeParam(it.spaceId, spaceId)
            writeParam(it.spaceType, spaceType)
        }
    suspend fun createComment(comment: NewComment): Outcome<CommentId> = transport.postApi(Api.Talk.CreateComment, comment)
    suspend fun updateComment(comment: UpdatedComment): Outcome<Boolean> = transport.postApi(Api.Talk.UpdateComment, comment)
    fun connectTalkLog(id: Uuid, space: SpaceType): Flow<TalkMessage> =
        transport.openEvents(
            Api.Talk.Connect,
            "id" to id.toString(),
            "space" to space.paramValue
        )
}
