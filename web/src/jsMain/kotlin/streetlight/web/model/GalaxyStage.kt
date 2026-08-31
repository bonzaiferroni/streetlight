package streetlight.web.model

import koala.model.dedup
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyContent
import streetlight.model.data.GalaxyPost
import streetlight.model.data.PostId

@Deprecated("Use refresh mechanism to reset stage")
class GalaxyStage(
    private val scope: CoroutineScope,
) {
    private val state = storeOf(GalaxyStageState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val postFlow = stateFlow.dedup { it.posts }

    fun setStage(content: GalaxyContent) {
        val posts = content.posts.sortedByDescending { it.base.createdAt } // td: implement other sorts
        state.update { it.copy(galaxy = content.galaxy, posts = posts) }
    }

    fun addPost(post: GalaxyPost) {
        val posts = stateNow.posts ?: emptyList()
        state.update { it.copy(posts = posts + post, isInitialStage = false) }
    }

    fun removePost(postId: PostId) {
        val posts = stateNow.posts ?: emptyList()
        state.update { it.copy(posts = posts.filter { item -> item.base.postId != postId }, isInitialStage = false)}
    }

    fun replacePost(post: GalaxyPost) {
        val posts = stateNow.posts?.map { if (it.base.postId == post.base.postId) post else it }
        state.update { it.copy(posts = posts, isInitialStage = false) }
    }
}

data class GalaxyStageState(
    val galaxy: Galaxy? = null,
    val posts: List<GalaxyPost>? = null,
    val isInitialStage: Boolean = true,
)