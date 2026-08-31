package streetlight.web.model

import kampfire.api.Markdown
import kampfire.model.Messenger
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import kampfire.utils.takeEllipsis
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.ChatPreview
import streetlight.model.data.Message
import streetlight.model.data.ReplyMessage
import streetlight.model.data.Star
import streetlight.web.io.ApiClient
import streetlight.web.io.OmniClient

class Inbox(
    private val scope: CoroutineScope,
    private val star: Star,
    initialChats: List<ChatPreview>,
    private val api: ApiClient,
    private val toaster: Toaster,
    omni: OmniClient,
) {
    private val state = storeOf(InboxState(initialChats, initialChats.firstOrNull()))

    val chatsState = state.tapOf { it.chats }
    val messagesState = state.tapOf { it.messages }
    val openChatState = state.tapOf { it.openChat }

    init {
        omni.lastRecordState.reactIn(scope) { omni ->
            val message = omni as? Message ?: return@reactIn
            state.set { copy(chats = chats.map { chat ->
                if (chat.chatId == message.chatId) {
                    val lastReadAt = chat.lastReadAt.takeIf { openChat == null || openChat.chatId != chat.chatId } ?: message.sentAt
                    chat.copy(
                        lastMessageAt = message.sentAt, lastReadAt = lastReadAt,
                        lastMessagePreview = message.content.value.takeEllipsis(40)
                    )
                } else chat
            })}

            updateChat(message)
        }

        state.now.openChat?.let {
            openChat(it)
        }
    }

    fun openChat(chat: ChatPreview) {
        state.set { copy(openChat = chat) }
        scope.launch(::openChat) {
            val messages = api.readChat(chat.chatId).toDataOr(toaster) { return@launch }
            state.set { copy(messages = messages) }
        }
    }

    fun sendReply(content: Markdown, messenger: Messenger) {
        val reply = ReplyMessage(
            chatId = state.now.openChat?.chatId ?: return,
            content = content.takeIf { it.value.isNotBlank() } ?: return
        )
        scope.launch(::sendReply) {
            messenger.deliverSending()
            api.sendMessage(reply).toDataOrNull(messenger, "Message sent.")
        }
    }

    private fun updateChat(message: Message) {
        if (state.now.openChat?.chatId != message.chatId) return
        state.set { copy(messages = listOf(message) + messages) }
    }
}

data class InboxState(
    val chats: List<ChatPreview>,
    val openChat: ChatPreview?,
    val messages: List<Message> = emptyList()
)