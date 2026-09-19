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

class TestPostClient: PostClient {
    override suspend fun createPost(post: PostEdit): Outcome<Post> = TODO()
    override suspend fun editPost(post: PostEdit): Outcome<Post> = TODO()
    override suspend fun readPosts(galaxyIds: List<GalaxyId>): Outcome<List<FeedEntity>> = TODO()
    override suspend fun readPosts(galaxyId: GalaxyId?, cursor: PostCursor?): Outcome<EntityFeed> = TODO()
    override suspend fun readPost(postId: PostId): Outcome<FeedEntity> = TODO()
    override suspend fun readMapPosts(query: MapQuery): Outcome<EntityFeed> = TODO()
    override suspend fun removePost(postId: PostId): Outcome<Boolean> = TODO()
    override suspend fun updateMark(update: MarkUpdate): Outcome<Unit> = TODO()
    override fun connectSpiritVision(): WebSocket = TODO()
}
