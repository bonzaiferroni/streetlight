package streetlight.web.ui

import kabinet.utils.toHourAndMinutesFormat
import kabinet.utils.toPastFormat
import kampfire.api.Markdown
import koala.css.*
import koala.dom.*
import koala.html.spacer
import kampfire.model.storeOf
import kampfire.model.tapOf
import koala.LottieFile
import koala.SvgFile
import koala.html.heading3
import koala.html.heading5
import streetlight.model.data.InboxContent
import streetlight.model.data.Star
import streetlight.model.data.StarBadge
import streetlight.model.ui.InboxRoute
import streetlight.web.io.OmniClient
import streetlight.web.model.Inbox

fun ViewScope.viewInbox(star: Star, content: InboxContent) {
    val omni = app.get<OmniClient>()
    val model = Inbox(scope, star, content.chats, api, toaster, omni)
    val isChatOpenState = model.openChatState.tapOf { it != null }

    column(modify(Height100Vh, Gap0)) {
        flowBlock(model.openChatState, modify(Height8)) { chat ->
            when (chat) {
                null -> box(modify(PlaceItemsCenter, Height100Pct)) {
                    heading3("Inbox", modify(OpacityHigh))
                }
                else -> row(modify(AlignItemsCenter, Height100Pct, ScaleIn)) {
                    val title = if (chat.subject != null) {
                        chat.subject
                    } else if (chat.badges.size == 2) {
                        chat.badges.firstOrNull { it.username != star.username }?.username?.value ?: "A Chat"
                    } else {
                        "Group Chat"
                    }
                    row(modify(Flex1, JustifyContentEnd)) {
                        button(SvgFile.ArrowLeft, { model.openChat(null) }, modify(Height5))
                    }
                    column(modify(Gap0, AlignItemsCenter)) {
                        heading5(title, modify(SingleLine))
                        textBlock(chat.createdAt.toHourAndMinutesFormat(), modify(TextSmall, OpacityHigh))
                    }
                    row(modify(Flex1, JustifyContentStart)) {
                        button(SvgFile.Archive)
                    }
                }
            }
        }
        row(modify(InboxStyle.Grid, Flex1, MinHeight0)) {
            chatList(model, star)
            messageList(model)
        }.flowModifier(isChatOpenState, Reveal, contentScope)
    }
}

fun RouteScope.viewInboxRoute() {
    routeBlock<InboxRoute, InboxContent> { content ->
        starGate { star ->
            viewInbox(star, content)
        }
    }
}

private fun ViewScope.chatList(model: Inbox, star: Star) {
    lazyColumn(
        list = model.chatList,
        mod = modify(InboxStyle.ChatList, Flex1, FlexColumn, Gap2Px, BorderRadius1),
        scrollState = model.chatScrollState
    ) { chat ->
        val isOpenState = model.openChatState.tapOf { it?.chatId == chat.chatId }
        flowBlock(isOpenState) { isOpen ->
            val isReadMod = if (isOpen || chat.isRead) null else Bold
            val isSelectedMod = if (isOpen) PrimaryCardBg else CardBg
            row(modify(BorderRadiusPillLeft, OverflowClip, Height7, Gap0, isReadMod, isSelectedMod)) {
                val usernames = chat.badges.filter { it.username != star.username }.joinToString(", ") { it.username.value }
                val badge = chat.badges.firstOrNull { it.username != star.username } ?: chat.badges.first()
                starBadge(badge)
                column(modify(Gap0, Padding1, JustifyContentCenter, Flex1)) {
                    row {
                        textBlock(chat.lastMessagePreview, modify(SingleLine, Flex1))
                        textBlock(chat.lastMessageAt.toPastFormat(), modify(OpacityHigh))
                    }
                    row(modify(TextSmall)) {
                        textBlock(usernames, modify(SingleLine, Flex1, MinWidth16, OpacityHigh))
                        chat.subject?.let {
                            textBlock(it, modify(SingleLine, OpacityHalf))
                        }
                    }
                }
            }.onClick {
                model.openChat(chat)
            }
        }
    }
}

fun AppendScope.starBadge(badge: StarBadge) {
    when (val thumb = badge.thumb) {
        null -> box(modify(Aspect1, BorderRadius50P, Outline, CardBg)) {
            span(badge.username.value.first().uppercase(), modify(PlaceSelfCenter, TextLarge, Bold))
        }
        else -> image(thumb, modify(Aspect1, BorderRadius50P, Outline))
    }
}

private fun ViewScope.messageList(model: Inbox) {
    flowBlock(model.openChatState, modify(InboxStyle.MessageList, Flex2, FlexColumn)) { chat ->
        if (chat == null) {
            box(modify(Height100Pct)) {
                lottie(LottieFile.Ghost, modify(PlaceSelfCenter, MaxHeight32, OpacityLow))
            }
            return@flowBlock
        }
        val messenger = MessageStore()

        lazyColumn(
            list = model.messageList,
            mod = modify(Flex1, PaddingTop1, FlexColumn, FlexReverse, BorderRadius1),
            scrollState = model.messageScrollState
        ) { message ->
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
                    if (model.sendReply(replyState.now, messenger)) {
                        replyState.set { Markdown.Empty }
                    }
                }
            }, messenger, modify(Zen))
        }
    }
}