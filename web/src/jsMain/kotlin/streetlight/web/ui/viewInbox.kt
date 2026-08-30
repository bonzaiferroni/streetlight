package streetlight.web.ui

import kampfire.api.Markdown
import kampfire.api.Username
import koala.css.*
import koala.dom.*
import koala.html.spacer
import koala.model.storeOf
import koala.model.tapOf
import streetlight.model.data.InboxContent
import streetlight.model.data.Star
import streetlight.model.ui.InboxRoute
import streetlight.web.io.OmniClient
import streetlight.web.model.Inbox
import kotlin.collections.forEachIndexed

fun ViewScope.viewInbox(star: Star, content: InboxContent) {
    val omni = app.get<OmniClient>()
    val model = Inbox(scope, star, content.chats, api, toaster, omni)

    row(modify(Height100Vh)) {
        flowBlock(model.chatsState, modify(Flex1, PaddingY1, OverflowYScroll)) { chats ->
            column(modify(Gap2Px, BorderRadius1, OverflowClip)) {
                chats.forEach { chat ->
                    val isOpenState = model.openChatState.tapOf { it?.chatId == chat.chatId }
                    flowBlock(isOpenState) { isOpen ->
                        val isReadMod = if (isOpen || chat.isRead) null else Bold
                        val isSelectedMod = if (isOpen) PrimaryCardBg else CardBg
                        column(modify(Padding1, Gap0, isReadMod, isSelectedMod)) {
                            val usernames = chat.usernames.filter { it != star.username }.joinToString(", ") { it.value }
                            textBlock(chat.lastMessagePreview, modify(SingleLine))
                            row(modify(OpacityHigh, TextSmall)) {
                                chat.subject?.let {
                                    textBlock(it, modify(SingleLine))
                                }
                                spacer(modify(Flex1))
                                textBlock(usernames, modify(SingleLine))
                            }
                        }.onClick {
                            model.openChat(chat)
                        }
                    }
                }
            }
        }
        flowBlock(model.messagesState, modify(Flex4, FlexColumn)) { messages ->
            val messenger = MessageStore()

            column(modify(Flex1, OverflowYScroll, PaddingTop1, FlexReverse)) {
                messages.forEachIndexed { index, message ->
                    card(modify(ZenBg, BorderRadius1, PaddingX2)) {
                        markdown(message.content)
                    }

                    val nextAuthor = messages.getOrNull(index + 1)?.author
                    if (message.author != nextAuthor) {
                        filigree {
                            textBlock(message.author.value, modify(TextSmall, OpacityHigh))
                        }
                    }
                }
            }

            column(modify(MinHeight16, MaxHeight50P, MarginBottom1)) {
                val replyState = storeOf(Markdown.Empty)
                styledMarkdownEditor(replyState, mod = modify(Flex1, OverflowYScroll))
                formSubmit("send", {
                    model.sendReply(replyState.now, messenger)
                }, messenger, modify(Zen))
            }
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