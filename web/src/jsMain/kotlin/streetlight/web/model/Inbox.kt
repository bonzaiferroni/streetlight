package streetlight.web.model

import kampfire.api.Markdown
import kampfire.model.LiveList
import kampfire.model.Messenger
import kampfire.model.ScrollState
import kampfire.model.toDataOr
import kampfire.utils.takeEllipsis
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import streetlight.model.data.ChatMessageRequest
import streetlight.model.data.ChatPreview
import streetlight.model.data.Message
import streetlight.model.data.RecordCursor
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
    private val state = storeOf(InboxState())

    val messageScrollState = storeOf<ScrollState?>(null)
    val chatScrollState = storeOf<ScrollState?>(null)

    val chatList = LiveList(initialChats) { it.chatId }
    val messageList = LiveList(emptyList<Message>()) { it.messageId }

    val openChatState = state.tapOf { it.openChat }

    init {
        omni.lastRecordState.reactIn(scope) { omni ->
            val message = omni as? Message ?: return@reactIn
            val openChat = openChatState.now

            chatList.liveItems.firstOrNull { it.chatId == message.chatId }?.let { chat ->
                val lastReadAt = chat.lastReadAt.takeIf { openChat == null || openChat.chatId != chat.chatId } ?: message.sentAt
                chatList.replace(chat.chatId, chat.copy(
                    lastMessageAt = message.sentAt, lastReadAt = lastReadAt,
                    lastMessagePreview = message.content.value.takeEllipsis(40)
                ))
            }

            updateChat(message)
        }

        scope.launch {
            launch {
                messageScrollState.flow.filterNotNull().collect { scroll ->
                    if (!scroll.atEnd) return@collect
                    if (state.now.atChatEnd) {
                        println("yer at the message end")
                    } else {
                        println("fetching more")
                        launch {
                            requestMessages()
                        }
                    }
                }
            }
            launch {
                chatScrollState.flow.filterNotNull().collect { scroll ->
                    if (scroll.atEnd) {
                        println("yer at the chat end")
                    }
                }
            }
        }
    }

    fun openChat(chat: ChatPreview?) {
        scope.launch(::openChat) {
            messageList.clear()
            state.set { copy(openChat = chat, atChatEnd = false, cursor = null) }
            requestMessages()
        }
    }

    suspend fun sendReply(content: Markdown, messenger: Messenger): Boolean {
        val reply = ReplyMessage(
            chatId = state.now.openChat?.chatId ?: return false,
            content = content.takeIf { it.value.isNotBlank() } ?: return false
        )
        api.sendMessage(reply).toDataOr(messenger) { return false }
        return true
    }

    private fun updateChat(message: Message) {
        if (state.now.openChat?.chatId != message.chatId) return
        scope.launch {
            messageList.insertAt(0, message)
        }
    }

    private suspend fun requestMessages() {
        val chat = state.now.openChat ?: return
        val requestCount = 15
        val messages = api.readChatMessages(ChatMessageRequest(chat.chatId, requestCount, state.now.cursor)).toDataOr(toaster) { return }
        val atEnd = messages.size < requestCount
        val cursor = messages.lastOrNull()?.let { RecordCursor(it.messageId.value, it.sentAt) }
        state.set { copy(atChatEnd = atEnd, cursor = cursor)}
        messageList.add(messages)
    }
}

data class InboxState(
    val openChat: ChatPreview? = null,
    val messageIndex: Int = 0,
    val atChatEnd: Boolean = false,
    val cursor: RecordCursor? = null
)