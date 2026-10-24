package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.MessageSocket
import kampfire.api.openSocket
import kampfire.api.getApi
import kampfire.api.postApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.*

/** The calls of posts and their marks, and the spirit socket. */
class PostClient(private val transport: ApiTransport) {
    suspend fun createPost(post: PostEdit): Outcome<Post> = transport.postApi(Api.Galaxies.CreatePost, post)
    suspend fun editPost(post: PostEdit): Outcome<Post> = transport.postApi(Api.Galaxies.UpdatePost, post)
    suspend fun readPosts(galaxyIds: List<GalaxyId>): Outcome<List<Entity>> = transport.postApi(Api.Galaxies.ReadMultiPosts, galaxyIds)
    suspend fun readPost(postId: PostId): Outcome<Entity> = transport.getApi(Api.Galaxies.ReadPostId, postId)
    suspend fun readMapPosts(query: MapQuery): Outcome<EntityFeed> = transport.getApi(Api.Posts.ReadMapQuery) { it.writeMapQuery(query) }
    suspend fun removePost(postId: PostId): Outcome<Boolean> = transport.postApi(Api.Galaxies.RemovePost, postId)
    suspend fun updateMark(update: MarkUpdate): Outcome<Unit> = transport.postApi(Api.Galaxies.UpdateMark, update)
    fun connectSpiritVision(): MessageSocket<SpiritFrame, SpiritFrame> = transport.openSocket(Api.Posts.SpiritVision)
}
