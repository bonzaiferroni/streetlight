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

class TestMessageClient: MessageClient {
    override suspend fun sendMessage(message: NewMessage): Outcome<Unit> = TODO()
    override suspend fun sendMessage(message: ReplyMessage): Outcome<Unit> = TODO()
    override suspend fun readInbox(): Outcome<InboxContent> = TODO()
    override suspend fun readChatMessages(request: ChatMessageRequest): Outcome<List<Message>> = TODO()
    override suspend fun readChats(request: ChatRequest): Outcome<List<ChatPreview>> = TODO()
    override suspend fun archiveChat(chatId: ChatId): Outcome<Unit> = TODO()
    override suspend fun unarchiveChat(chatId: ChatId): Outcome<Unit> = TODO()
    override suspend fun readChatPreview(chatId: ChatId): Outcome<ChatPreview> = TODO()
    override fun connectChat(scope: CoroutineScope): WebChatSocket = TODO()
}
