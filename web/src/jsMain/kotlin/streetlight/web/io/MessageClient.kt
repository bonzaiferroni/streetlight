package streetlight.web.io

import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.model.AccountUpgradeRequest
import kampfire.model.EmailChange
import kampfire.model.GeoPoint
import kampfire.model.GeoRect
import kampfire.model.LoginRequest
import kampfire.model.Outcome
import kampfire.model.PasswordChange
import kampfire.model.PasswordVerification
import kampfire.model.SignUpRequest
import kampfire.model.Url
import koala.Image
import koala.model.DocId
import koala.model.DocTableItem
import kotlinx.coroutines.CoroutineScope
import streetlight.model.Api
import streetlight.model.data.*
import streetlight.model.writeCursor
import web.sockets.WebSocket
import web.sse.EventSource
import kotlin.uuid.Uuid

/** The calls of `Api.Messages`, and the chat socket. */
interface MessageClient {
    suspend fun sendMessage(message: NewMessage): Outcome<Unit>
    suspend fun sendMessage(message: ReplyMessage): Outcome<Unit>
    suspend fun readInbox(): Outcome<InboxContent>
    suspend fun readChatMessages(request: ChatMessageRequest): Outcome<List<Message>>
    suspend fun readChats(request: ChatRequest): Outcome<List<ChatPreview>>
    suspend fun archiveChat(chatId: ChatId): Outcome<Unit>
    suspend fun unarchiveChat(chatId: ChatId): Outcome<Unit>
    suspend fun readChatPreview(chatId: ChatId): Outcome<ChatPreview>
    fun connectChat(scope: CoroutineScope): WebChatSocket
}

class BrowserMessageClient(private val client: FetchClient): MessageClient {
    override suspend fun sendMessage(message: NewMessage) = client.postApi(Api.Messages.SendNew, message)
    override suspend fun sendMessage(message: ReplyMessage) = client.postApi(Api.Messages.SendReply, message)
    override suspend fun readInbox() = client.getApi(Api.Messages.Inbox)
    override suspend fun readChatMessages(request: ChatMessageRequest) = client.postApi(Api.Messages.ReadChatMessages, request)
    override suspend fun readChats(request: ChatRequest) = client.postApi(Api.Messages.ReadChats, request)
    override suspend fun archiveChat(chatId: ChatId) = client.postApi(Api.Messages.ArchiveChat, chatId)
    override suspend fun unarchiveChat(chatId: ChatId) = client.postApi(Api.Messages.UnarchiveChat, chatId)
    override suspend fun readChatPreview(chatId: ChatId) = client.getApi(Api.Messages.ReadChatPreview, chatId)
    override fun connectChat(scope: CoroutineScope) = WebChatSocket(client.connectSocket(Api.GroupChat), scope)
}
