package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Galaxy
import streetlight.model.data.Post
import streetlight.model.data.PostId
import streetlight.web.shells.GalaxyContent
import streetlight.web.ui.ViewModel

class GalaxyStage(
    private val scope: CoroutineScope,
) {
    private val state = storeOf(GalaxyStageState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val postFlow = stateFlow.mapDistinct { it.posts }

    fun setStage(content: GalaxyContent) {
        state.set { it.copy(galaxy = content.galaxy, posts = content.posts) }
    }

    fun addPost(post: Post) {
        val posts = stateNow.posts ?: emptyList()
        state.set { it.copy(posts = posts + post) }
    }

    fun removePost(postId: PostId) {
        val posts = stateNow.posts ?: emptyList()
        state.set { it.copy(posts = posts.filter { item -> item.postId == postId })}
    }
}

data class GalaxyStageState(
    val galaxy: Galaxy? = null,
    val posts: List<Post>? = null,
)