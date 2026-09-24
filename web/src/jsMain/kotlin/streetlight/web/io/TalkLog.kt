package streetlight.web.io

import kampfire.api.Markdown
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.*
import streetlight.web.ui.CommentView
import kotlin.uuid.Uuid

/** The live comments of a space: its history, and the comments created and updated as they arrive. */
class TalkLog(
    private val scope: CoroutineScope,
    private val spaceId: Uuid,
    private val spaceType: SpaceType,
    private val api: ApiClient,
) {
    private val client: SSEClient<TalkMessage> = sseClientOf(scope) {
        api.talk.connectTalkLog(spaceId, spaceType)
    }

    private val state = storeOf(TalkLogState())

    val stateFlow = state.flow
    val stateNow get() = state.now
    val sortByField = state.mutableTapOf({ it.sortBy }) { copy(sortBy = it) }

    private val _messageFlow = MutableSharedFlow<TalkMessage>()
    val messageFlow: Flow<TalkMessage> = _messageFlow

    val commentViews = mutableMapOf<CommentId, CommentView>()

    val comments = mutableListOf<Comment>()

    init {


        scope.launch("collect messages") {
            client.messageFlow.collect {
                takeMessage(it)
            }
        }

        scope.coroutineContext[Job]?.invokeOnCompletion {
            client.disconnect()
        }

        client.connect()
    }

    /** The comments of the space so far. */
    suspend fun readHistory() = api.talk.readHistory(spaceId, spaceType)

    /** Sorts the comments by [value]. */
    fun setSortBy(value: PostOrder) {
        commentViews.clear() // is this a memory leak? we need to cancel a supervisor job
        state.update { it.copy(sortBy = value) }
    }

    /** Changes the text of the comment [commentId], returning whether it changed, or `null` on a problem. */
    suspend fun updateComment(commentId: CommentId, text: Markdown): Boolean? {
        val response = api.talk.updateComment(UpdatedComment(
            commentId = commentId,
            spaceId = spaceId,
            text = text
        )) ?: return null
        return when (response) {
            is Ok -> response.data
            is Problem -> null
        }
    }

    /** Adds a comment, as a reply to [parentId] when given, returning its id, or `null` on a problem. */
    suspend fun createComment(parentId: CommentId?, text: Markdown): CommentId? {
        val response = api.talk.createComment(NewComment(
            spaceId = spaceId,
            spaceType = spaceType,
            parentId = parentId,
            text = text
        )) ?: return null
        return when (response) {
            is Ok -> response.data
            is Problem -> null
        }
    }

    private fun takeMessage(message: TalkMessage) {
        when (message) {
            is CommentCreated -> {
                comments.add(message.comment)
            }
            is CommentUpdated -> {
                // hmmm
            }
        }

        scope.launch {
            _messageFlow.emit(message)
        }
    }
}

/** The sort order of a [TalkLog]. */
data class TalkLogState(
    val sortBy: PostOrder = PostOrder.Old,
)