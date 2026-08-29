package streetlight.web.ui

import kampfire.api.Markdown
import kampfire.model.toDataOrNull
import koala.css.*
import koala.dom.*
import koala.html.heading4
import koala.html.heading6
import koala.model.storeOf
import streetlight.model.data.InboxContent
import streetlight.model.data.MessageId
import streetlight.model.data.Star
import streetlight.model.ui.InboxRoute
import streetlight.web.model.Inbox
import kotlin.uuid.Uuid

fun ViewScope.viewInbox(star: Star, content: InboxContent) {
    val model = Inbox(scope, star, content.chats, api, toaster)

    row {
        column(modify(Flex1)) {
            content.chats.forEach { chat ->
                val isReadMod = if (chat.isRead) null else Bold
                row(modify(Height5, AlignItemsCenter, isReadMod)) {
                    val usernames = chat.usernames.filter { it != star.username }.joinToString(", ") { it.value }
                    textBlock(usernames)
                    chat.subject?.let {
                        textBlock(it)
                    }
                }.onClick {
                    model.openChat(chat)
                }
            }
        }
        flowBlock(model.messagesState, modify(Magic, Flex4)) { messages ->
            val messenger = MessageStore()

            messages.forEach { message ->
                column {
                    textBlock(message.author.value)
                    markdown(message.content)

                }
            }

            filigree(modify(MarginTop2)) {
                heading6("reply")
            }

            val replyState = storeOf(Markdown.Empty)
            styledMarkdownEditor(replyState, "reply", modify(MinHeight32))
            formSubmit("send", {
                model.sendReply(replyState.now, messenger)
            }, messenger)
        }
    }
}

fun RouteScope.viewInboxRoute() {
    routeBlock<InboxRoute, InboxContent> { content ->
        starGate { star ->
            viewInbox(star, content)
        }
    }
}