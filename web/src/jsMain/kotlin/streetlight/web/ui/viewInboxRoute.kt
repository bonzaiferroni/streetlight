package streetlight.web.ui

import kampfire.api.Markdown
import kampfire.model.toDataOrNull
import koala.css.*
import koala.dom.*
import koala.html.heading4
import koala.html.heading6
import koala.model.storeOf
import streetlight.model.data.InboxContent
import streetlight.model.data.Message
import streetlight.model.data.MessageEdit
import streetlight.model.data.MessageId
import streetlight.model.ui.InboxRoute
import kotlin.uuid.Uuid

fun ViewScope.viewInbox(content: InboxContent) {
    val viewedMessageState = storeOf<Message?>(null)

    row {
        column(modify(Flex1)) {
            content.messages.forEach { message ->
                val isReadMod = if (message.isRead) null else Bold
                row(modify(Height5, AlignItemsCenter, isReadMod)) {
                    textBlock(message.recipient.value)
                    message.subject?.let {
                        textBlock(it)
                    }
                }.onClick {
                    viewedMessageState.set(message)
                }
            }
        }
        flowBlock(viewedMessageState, modify(Magic, Flex4)) { message ->
            if (message == null) return@flowBlock
            val messenger = MessageStore()
            column {
                filigree {
                    heading4("from ${message.author}")
                }
                textBlock(message.author.value)
                message.subject?.let {
                    textBlock(it)
                }
                markdown(message.content)
                filigree(modify(MarginTop2)) {
                    heading6("reply")
                }

                val replyState = storeOf(Markdown.Empty)
                styledMarkdownEditor(replyState, "reply", modify(MinHeight32))
                formSubmit("send", {
                    val content = replyState.now.takeIf { it.value.isNotBlank() } ?: return@formSubmit
                    val edit = MessageEdit(
                        messageId = MessageId(Uuid.random()), chainId = message.chainId, parentId = message.messageId,
                        recipient = message.author, subject = message.subject?.let { "RE: $it"}, content = content
                    )
                    launchEffect {
                        api.sendMessage(edit).toDataOrNull()
                    }
                }, messenger)
            }
        }
    }
}

fun RouteScope.viewInboxRoute() {
    routeBlock<InboxRoute, InboxContent> { content ->
        starGate {
            viewInbox(content)
        }
    }
}