package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.data.*
import web.sockets.WebSocket

class TestPostClient: PostClient {
    override suspend fun createPost(post: PostEdit): Outcome<Post> = TODO()
    override suspend fun editPost(post: PostEdit): Outcome<Post> = TODO()
    override suspend fun readPosts(galaxyIds: List<GalaxyId>): Outcome<List<Entity>> = TODO()
    override suspend fun readPosts(galaxyId: GalaxyId?, cursor: PostCursor?): Outcome<EntityFeed> = TODO()
    override suspend fun readPost(postId: PostId): Outcome<Entity> = TODO()
    override suspend fun readMapPosts(query: MapQuery): Outcome<EntityFeed> = TODO()
    override suspend fun removePost(postId: PostId): Outcome<Boolean> = TODO()
    override suspend fun updateMark(update: MarkUpdate): Outcome<Unit> = TODO()
    override fun connectSpiritVision(): WebSocket = TODO()
}
