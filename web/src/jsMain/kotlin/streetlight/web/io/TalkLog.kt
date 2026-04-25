package streetlight.web.io

import kampfire.api.StringId
import kampfire.model.Ok
import kampfire.model.Problem
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.*

class TalkLog(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val spaceId: StringId,
    private val spaceType: SpaceType,
) {
    private val client: SocketClient<TalkMessage, TalkRequest> = socketRequestClientOf(scope) {
        api.connectTalkLog(spaceId, spaceType)
    }

    private val state = storeOf(TalkLogState())

    val stateFlow = state.flow
    val stateNow get() = state.now

    private val _messageFlow = MutableSharedFlow<TalkMessage>()
    val messageFlow: Flow<TalkMessage> = _messageFlow

    init {
        scope.launch {
            client.messageFlow.collect {
                takeMessage(it)
            }
        }

        scope.coroutineContext[Job]?.invokeOnCompletion {
            client.disconnect()
        }

        client.connect()

    }

    suspend fun sendComment(parentId: CommentId?, text: String): CommentId? {
        val response = api.createComment(NewComment(
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
        scope.launch {
            _messageFlow.emit(message)
        }
    }
}

data class TalkLogState(
    val starCount: Int = 0,
)