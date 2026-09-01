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
import streetlight.model.data.ChatId
import streetlight.model.data.ChatMessageRequest
import streetlight.model.data.ChatPreview
import streetlight.model.data.ChatRequest
import streetlight.model.data.Message
import streetlight.model.data.RecordCursor
import streetlight.model.data.ReplyMessage
import streetlight.model.data.Star
import streetlight.model.data.limitOrDefault
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
    val isArchiveState = state.tapOf { it.isArchive }

    val chatList = LiveList(initialChats) { it.chatId }
    val messageList = LiveList(emptyList<Message>()) { it.messageId }
    val chatScrollState = storeOf<ScrollState?>(null)
    val messageScrollState = storeOf<ScrollState?>(null)
    val chatCursorState = storeOf(CursorState())
    val messageCursorState = storeOf(CursorState())

    val openChatState = state.tapOf { it.openChat }

    init {
        omni.lastRecordState.reactIn(scope) { omni ->
            val message = (omni as? Message) ?: return@reactIn
            val openChat = openChatState.now

            chatList.liveItems.firstOrNull { it.chatId == message.chatId }?.let { chat ->
                val lastReadAt = chat.lastReadAt.takeIf { openChat == null || openChat.chatId != chat.chatId } ?: message.sentAt
                chatList.remove(chat.chatId)
                chatList.insertAt(0, chat.copy(
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
                    launch {
                        requestMoreMessages()
                    }
                }
            }
            launch {
                chatScrollState.flow.filterNotNull().collect { scroll ->
                    if (!scroll.atEnd) return@collect
                    launch {
                        requestMoreChats()
                    }
                }
            }
        }
    }

    fun openChat(chat: ChatPreview?) {
        scope.launch(::openChat) {
            messageList.clear()
            state.set { copy(openChat = chat) }
            messageCursorState.set { CursorState() }
            requestMoreMessages()
        }
    }

    fun setIsArchive(isArchive: Boolean) {
        if (isArchive == state.now.isArchive) return
        scope.launch(::setIsArchive) {
            chatList.clear()
            state.set { copy(isArchive = isArchive) }
            chatCursorState.set { CursorState() }
            requestMoreChats()
        }
    }

    fun toggleArchive() {
        val chatId = state.now.openChat?.chatId ?: return
        scope.launch {
            when (state.now.isArchive) {
                true -> api.unarchiveChat(chatId).toDataOr(toaster) { return@launch }
                else -> api.archiveChat(chatId).toDataOr(toaster) { return@launch }
            }
            val index = chatList.liveItems.indexOfFirst { it.chatId == chatId }.takeIf { it < chatList.liveItems.size - 1 }
                ?: chatList.liveItems.size.takeIf { it > 1 }?.let { it - 2 }
            chatList.remove(chatId)
            val nextChat = index?.let { chatList.liveItems.getOrNull(index) }
            openChat(nextChat)
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

    private suspend fun requestMoreMessages() {
        val chat = state.now.openChat ?: return
        val cursorState = messageCursorState.now
        if (cursorState.isCompleted || cursorState.isFetching) return
        messageCursorState.set { copy(isFetching = true) }
        val cursor = messageList.liveItems.lastOrNull()?.let { RecordCursor(it.messageId.value, it.sentAt) }

        val messages = api.readChatMessages(ChatMessageRequest(chat.chatId, cursor)).toDataOr(toaster) { return }

        val isCompleted = messages.size < cursor.limitOrDefault
        messageCursorState.set { CursorState(isCompleted, false)}
        messageList.add(messages)
    }

    private suspend fun requestMoreChats() {
        val cursorState = chatCursorState.now
        if (cursorState.isCompleted || cursorState.isFetching) return
        chatCursorState.set { copy(isFetching = true) }
        val cursor = chatList.liveItems.lastOrNull()?.let { RecordCursor(it.chatId.value, it.lastMessageAt) }

        val chats = api.readChats(ChatRequest(state.now.isArchive, cursor)).toDataOr(toaster) { return }

        val isCompleted = chats.size < cursor.limitOrDefault
        chatCursorState.set { CursorState(isCompleted, false) }
        chatList.add(chats)
    }
}

data class InboxState(
    val openChat: ChatPreview? = null,
    val messageIndex: Int = 0,
    val isArchive: Boolean = false,
)

data class CursorState(
    val isFetching: Boolean = false,
    val isCompleted: Boolean = false,
)