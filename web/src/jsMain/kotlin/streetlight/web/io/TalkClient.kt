package streetlight.web.io

import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
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
import kotlinx.coroutines.CoroutineScope
import streetlight.model.Api
import streetlight.model.data.*
import streetlight.model.writeCursor
import web.sockets.WebSocket
import web.sse.EventSource
import kotlin.uuid.Uuid

/** The calls of `Api.Talk`, and the talk event stream. */
interface TalkClient {
    suspend fun readHistory(spaceId: Uuid, spaceType: SpaceType): Outcome<List<Comment>>
    suspend fun createComment(comment: NewComment): Outcome<CommentId>
    suspend fun updateComment(comment: UpdatedComment): Outcome<Boolean>
    fun connectTalkLog(id: Uuid, space: SpaceType): EventSource
}

class BrowserTalkClient(private val client: FetchClient): TalkClient {
    override suspend fun readHistory(spaceId: Uuid, spaceType: SpaceType) =
        client.getApi(Api.Talk.ReadHistory) {
            writeParam(it.spaceId, spaceId)
            writeParam(it.spaceType, spaceType)
        }
    override suspend fun createComment(comment: NewComment) = client.postApi(Api.Talk.CreateComment, comment)
    override suspend fun updateComment(comment: UpdatedComment) = client.postApi(Api.Talk.UpdateComment, comment)
    override fun connectTalkLog(id: Uuid, space: SpaceType) =
        client.connectSSE(
            Api.Talk.Connect,
            "id" to id.toString(),
            "space" to space.paramValue
        )
}
