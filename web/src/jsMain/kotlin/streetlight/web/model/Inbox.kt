package streetlight.web.model

import kampfire.api.Markdown
import kampfire.model.CursorStatus
import kampfire.model.LiveList
import kampfire.model.Messenger
import kampfire.model.ScrollState
import kampfire.model.toDataOr
import kampfire.utils.takeEllipsis
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOrNull
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import streetlight.model.data.ChatId
import streetlight.model.data.ChatMessageRequest
import streetlight.model.data.ChatPreview
import streetlight.model.data.ChatRequest
import streetlight.model.data.Message
import kampfire.model.TimeCursor
import streetlight.model.data.ReplyMessage
import streetlight.model.data.Star
import kampfire.model.requestWithCursor
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
    val chatCursorState = storeOf(CursorStatus())
    val messageCursorState = storeOf(CursorStatus())

    val openChatState = state.tapOf { it.openChat }

    init {
        omni.lastRecordState.reactIn(scope) { omni ->
            val message = (omni as? Message) ?: return@reactIn
            consumeNewMessage(message)
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
            messageCursorState.set { CursorStatus() }
            requestMoreMessages()
        }
    }

    fun setIsArchive(isArchive: Boolean) {
        if (isArchive == state.now.isArchive) return
        scope.launch(::setIsArchive) {
            chatList.clear()
            state.set { copy(isArchive = isArchive) }
            chatCursorState.set { CursorStatus() }
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

    private fun updateMessages(message: Message) {
        if (state.now.openChat?.chatId != message.chatId) return
        scope.launch {
            messageList.insertAt(0, message)
        }
    }

    private suspend fun requestMoreMessages() {
        val chat = state.now.openChat ?: return
        requestWithCursor(messageCursorState, messageList, { TimeCursor(it.messageId.value, it.sentAt) }) {
            api.readChatMessages(ChatMessageRequest(chat.chatId, it)).toDataOrNull(toaster)
        }
    }

    private suspend fun requestMoreChats() {
        requestWithCursor(chatCursorState, chatList, { TimeCursor(it.chatId.value, it.lastMessageAt) }) {
            api.readChats(ChatRequest(state.now.isArchive, it)).toDataOrNull(toaster)
        }
    }

    private suspend fun consumeNewMessage(message: Message) {
        val openChat = openChatState.now

        chatList.liveItems.firstOrNull { it.chatId == message.chatId }.let { cachedChat ->
            if (cachedChat == null) {
                if (state.now.isArchive) return@let
                fetchPreview(message.chatId)
            }
            val chat = cachedChat ?: ChatPreview(
                chatId = message.chatId,
                badges = emptyList(),
                subject = null,
                lastMessagePreview = message.content.value.takeEllipsis(40),
                lastMessageAt = message.sentAt,
                lastReadAt = null,
                archivedAt = null,
                createdAt = message.sentAt
            )
            val lastReadAt = chat.lastReadAt.takeIf { openChat == null || openChat.chatId != chat.chatId } ?: message.sentAt
            chatList.remove(chat.chatId)
            chatList.insertAt(0, chat.copy(
                lastMessageAt = message.sentAt, lastReadAt = lastReadAt,
                lastMessagePreview = message.content.value.takeEllipsis(40)
            ))
        }

        updateMessages(message)
    }

    private fun fetchPreview(chatId: ChatId) {
        scope.launch {
            val chat = api.readChatPreview(chatId).toDataOr(toaster) { return@launch }
            chatList.replace(chat)
        }
    }
}

data class InboxState(
    val openChat: ChatPreview? = null,
    val isArchive: Boolean = false,
)


