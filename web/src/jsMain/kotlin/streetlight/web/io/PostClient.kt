package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.*
import streetlight.model.writeCursor
import web.sockets.WebSocket

/** The calls of posts and their marks, and the spirit socket. */
interface PostClient {
    suspend fun createPost(post: PostEdit): Outcome<Post>
    suspend fun editPost(post: PostEdit): Outcome<Post>
    suspend fun readPosts(galaxyIds: List<GalaxyId>): Outcome<List<Entity>>
    suspend fun readPosts(galaxyId: GalaxyId?, cursor: EntityCursor? = null): Outcome<EntityFeed>
    suspend fun readPost(postId: PostId): Outcome<Entity>
    suspend fun readMapPosts(query: MapQuery): Outcome<EntityFeed>
    suspend fun removePost(postId: PostId): Outcome<Boolean>
    suspend fun updateMark(update: MarkUpdate): Outcome<Unit>
    fun connectSpiritVision(): WebSocket
}

class BrowserPostClient(private val client: FetchClient): PostClient {
    override suspend fun createPost(post: PostEdit) = client.postApi(Api.Galaxies.CreatePost, post)
    override suspend fun editPost(post: PostEdit) = client.postApi(Api.Galaxies.UpdatePost, post)
    override suspend fun readPosts(galaxyIds: List<GalaxyId>) = client.postApi(Api.Galaxies.ReadMultiPosts, galaxyIds)
    override suspend fun readPosts(galaxyId: GalaxyId?, cursor: EntityCursor?) =
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
