package streetlight.web.model

import kampfire.api.Markdown
import kampfire.model.Messenger
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import koala.model.storeOf
import koala.model.tapOf
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.ChatPreview
import streetlight.model.data.Message
import streetlight.model.data.MessageId
import streetlight.model.data.ReplyMessage
import streetlight.model.data.Star
import streetlight.web.io.ApiClient
import kotlin.uuid.Uuid

class Inbox(
    private val scope: CoroutineScope,
    private val star: Star,
    initialChats: List<ChatPreview>,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(InboxState())

    val messagesState = state.tapOf { it.messages }

    fun openChat(chat: ChatPreview) {
        state.set { copy(chat = chat) }
        scope.launch(::openChat) {
            val messages = api.readChat(chat.chatId).toDataOr(toaster) { return@launch }
            state.set { copy(messages = messages) }
        }
    }

    fun sendReply(content: Markdown, messenger: Messenger) {
        val reply = ReplyMessage(
            chatId = state.now.chat?.chatId ?: return,
            content = content.takeIf { it.value.isNotBlank() } ?: return
        )
        scope.launch(::sendReply) {
            api.sendMessage(reply).toDataOrNull(messenger)
        }
    }
}

data class InboxState(
    val chat: ChatPreview? = null,
    val messages: List<Message> = emptyList()
)