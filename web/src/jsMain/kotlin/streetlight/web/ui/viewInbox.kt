package streetlight.web.ui

import kampfire.api.Markdown
import koala.css.*
import koala.dom.*
import koala.html.heading4
import koala.html.iconLogo
import koala.html.spacer
import kampfire.model.storeOf
import kampfire.model.tapOf
import streetlight.model.data.InboxContent
import streetlight.model.data.Star
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.InboxRoute
import streetlight.web.io.OmniClient
import streetlight.web.model.Inbox
import kotlin.collections.forEachIndexed

fun ViewScope.viewInbox(star: Star, content: InboxContent) {
    val omni = app.get<OmniClient>()
    val model = Inbox(scope, star, content.chats, api, toaster, omni)

    row(modify(Height100Vh)) {
        column(modify(Gap0, Flex1)) {
            row(modify(AlignItemsCenter, JustifyContentCenter, PaddingTop1, Gap0)) {
                navigation(HomeRoute, modify(GlowShadow, FlexRow)) {
                    iconLogo()
                }
                heading4("Messages", modify(OpacityHigh))
            }
            lazyColumn(model.chatList, modify(Flex1, FlexColumn, Gap2Px, BorderRadius1)) { chat ->
                println("rebuilding")
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
        flowBlock(model.openChatState, modify(Flex3, FlexColumn)) {
            val messenger = MessageStore()

            lazyColumn(model.messageList, modify(Flex1, PaddingTop1, FlexColumn, FlexReverse)) { message ->
                val index = model.messageList.liveItems.indexOf(message)
                val nextAuthor = model.messageList.liveItems.getOrNull(index + 1)?.author
                if (message.author != nextAuthor) {
                    filigree(modify(MarginBottom1)) {
                        textBlock(message.author.value, modify(TextSmall, OpacityHigh))
                    }
                }

                card(modify(ZenBg, BorderRadius1, PaddingX2, FadeIn)) {
                    markdown(message.content)
                }
            }

            column(modify(MinHeight16, MaxHeight50P, MarginBottom1)) {
                val replyState = storeOf(Markdown.Empty)
                styledMarkdownEditor(replyState, mod = modify(Flex1, OverflowYScroll))
                formSubmit("send", {
                    launchEffect {
                        messenger.deliverSending()
                        if (model.sendReply(replyState.now, messenger)) {
                            replyState.set { Markdown.Empty }
                        }
                    }
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