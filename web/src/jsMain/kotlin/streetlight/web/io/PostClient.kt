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

interface PostClient {
    suspend fun createPost(post: PostEdit): Outcome<Post>
    suspend fun editPost(post: PostEdit): Outcome<Post>
    suspend fun readPosts(galaxyIds: List<GalaxyId>): Outcome<List<FeedEntity>>
    suspend fun readPosts(galaxyId: GalaxyId?, cursor: PostCursor? = null): Outcome<EntityFeed>
    suspend fun readPost(postId: PostId): Outcome<FeedEntity>
    suspend fun readMapPosts(query: MapQuery): Outcome<EntityFeed>
    suspend fun removePost(postId: PostId): Outcome<Boolean>
    suspend fun updateMark(update: MarkUpdate): Outcome<Unit>
    fun connectSpiritVision(): WebSocket
}

class BrowserPostClient(private val client: FetchClient): PostClient {
    override suspend fun createPost(post: PostEdit) = client.postApi(Api.Galaxies.CreatePost, post)
    override suspend fun editPost(post: PostEdit) = client.postApi(Api.Galaxies.UpdatePost, post)
    override suspend fun readPosts(galaxyIds: List<GalaxyId>) = client.postApi(Api.Galaxies.ReadMultiPosts, galaxyIds)
    override suspend fun readPosts(galaxyId: GalaxyId?, cursor: PostCursor?) =
        client.getApi(Api.Posts.ReadFeed) {
            writeParam(it.galaxyId, galaxyId)
            writeCursor(Api.Posts.ReadFeed, cursor)
        }
    override suspend fun readPost(postId: PostId) = client.getApi(Api.Galaxies.ReadPostId, postId)
    override suspend fun readMapPosts(query: MapQuery) = client.getApi(Api.Posts.ReadMapQuery) { writeMapQuery(query) }
    override suspend fun removePost(postId: PostId) = client.postApi(Api.Galaxies.RemovePost, postId)
    override suspend fun updateMark(update: MarkUpdate) = client.postApi(Api.Galaxies.UpdateMark, update)
    override fun connectSpiritVision() = client.connectSocket(Api.Posts.SpiritVision)
}
