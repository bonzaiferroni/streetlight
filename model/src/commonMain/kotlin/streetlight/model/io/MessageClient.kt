package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.MessageSocket
import kampfire.api.openSocket
import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.api.getApi
import kampfire.api.postApi
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
import kotlin.uuid.Uuid
import streetlight.model.Api
import streetlight.model.data.*

/** The calls of `Api.Messages`, and the chat socket. */
class MessageClient(private val transport: ApiTransport) {
    suspend fun sendMessage(message: NewMessage): Outcome<Unit> = transport.postApi(Api.Messages.SendNew, message)
    suspend fun sendMessage(message: ReplyMessage): Outcome<Unit> = transport.postApi(Api.Messages.SendReply, message)
    suspend fun readInbox(): Outcome<InboxContent> = transport.getApi(Api.Messages.Inbox)
    suspend fun readChatMessages(request: ChatMessageRequest): Outcome<List<Message>> = transport.postApi(Api.Messages.ReadChatMessages, request)
    suspend fun readChats(request: ChatRequest): Outcome<List<ChatPreview>> = transport.postApi(Api.Messages.ReadChats, request)
    suspend fun archiveChat(chatId: ChatId): Outcome<Unit> = transport.postApi(Api.Messages.ArchiveChat, chatId)
    suspend fun unarchiveChat(chatId: ChatId): Outcome<Unit> = transport.postApi(Api.Messages.UnarchiveChat, chatId)
    suspend fun readChatPreview(chatId: ChatId): Outcome<ChatPreview> = transport.getApi(Api.Messages.ReadChatPreview, chatId)
    fun connectChat(): MessageSocket<ChatMessage, ChatMessage> = transport.openSocket(Api.GroupChat)
}
