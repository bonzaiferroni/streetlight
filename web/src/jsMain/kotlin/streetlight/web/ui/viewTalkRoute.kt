package streetlight.web.ui

import kabinet.utils.toAgoFormat
import kampfire.api.StringId
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.html.image
import koala.html.navigationIfNotNull
import koala.html.spacer
import koala.model.storeOf
import kotlinx.browser.document
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import streetlight.model.data.Comment
import streetlight.model.data.CommentId
import streetlight.model.data.TalkHistory
import streetlight.model.data.SpaceType
import streetlight.model.data.TalkComment
import streetlight.web.StarRoute
import streetlight.web.TalkRoute
import streetlight.web.io.TalkLog
import streetlight.web.model.Streetlight

private val renderedElements = mutableMapOf<CommentId, CommentView>()

fun ViewContext<TalkLog>.viewTalkLog(stringId: StringId, type: SpaceType) {
    renderedElements.clear()

    var root: HTMLElement? = null

    column {
        filigree { heading1("Talk") }
        commentEditor("comment", null)
        root = column { }
    }

    renderScope.launch {
        model.messageFlow.collect { message ->
            when (message) {
                is TalkHistory -> {
                    buildTree(root!!, message)
                }
                is TalkComment -> {
                    growTree(root!!, message.comment)
                }
            }
        }
    }
}

fun ViewContext<Streetlight>.viewTalkRoute() {
    routeBlock<TalkRoute> { route ->
        val model = TalkLog(renderScope, api, route.stringId, route.type)
        viewContextOf(model) {
            viewTalkLog(route.stringId, route.type)
        }
    }
}

fun ViewContext<TalkLog>.buildTree(container: HTMLElement, history: TalkHistory) {
    val comments = history.comments.sortedBy { it.createdAt }

    replaceRender(container) {
        comments.forEach {
            addComment(it, history.comments)
        }
    }
}

fun ViewContext<TalkLog>.growTree(container: HTMLElement, comment: Comment) {
    console.log(comment.parentId)
    val container = comment.parentId?.let { parentId ->
        renderedElements[parentId]?.also { it.body.modify(TalkLogClass.HasNestedContent) }?.childColumn
    } ?: container

    prependRender(container) {
        addComment(comment, emptyList())
    }
}

fun ViewContext<TalkLog>.addComment(comment: Comment, comments: List<Comment>) {
    if (renderedElements.contains(comment.commentId)) return

    var childColumn: HTMLElement? = null
    var replyElement: HTMLElement? = null
    var rootElement: HTMLElement? = null

    var hasChildren = false

    fun startReply() {
        val replyElement = replyElement ?: error("reply element not found")
        if (!replyElement.isModified(DisplayNone)) return
        replyElement.unmodify(DisplayNone)
        rootElement?.modify(TalkLogClass.HasNestedContent)

        document.startViewTransition {
            replaceRender(replyElement) {
                commentEditor("reply", comment.commentId, modify(AutoMagic, SlideLeft)) { commentId ->
                    if (commentId != null) {
                        replyElement.modify(DisplayNone)
                    }
                }
            }
        }
    }

    val controlMod = modify(BorderRadius1, FlexDirectionRow, AlignItemsCenter)

    rootElement = column(modify(TalkLogClass.Comment, Gap0)) {
        card(modify(ZenCardBg, Gap0, Padding0, OverflowClip, AutoMagic)) {
            row(modify(AlignItemsCenter, modify(ZenCardBg, Padding1))) {
                navigationIfNotNull(comment.username?.let { StarRoute(it) }) {
                    row(modify(AlignItemsCenter)) {
                        image(comment.thumb, modify(Aspect1, Height6, BorderRadius50P))
                        column(modify(Gap0)) {
                            textBlock(comment.username ?: "Someone")
                            textBlock(comment.createdAt.toAgoFormat(), modify(OpacityMost, SmallText))
                        }
                    }
                }
                spacer(modify(Flex1))
                row(modify(JustifySelfEnd, AlignItemsCenter)) {
                    card(controlMod) {
                        textBlock(comment.lightCount.toString())
                        icon(SvgFile.Flame, modify(Height4))
                    }

                    card(controlMod) {
                        textBlock(comment.replyCount.toString())
                        icon(SvgFile.Reply, modify(Height4))
                    }
                }
            }
            column(modify(Padding1)) {
                markdown(comment.text, modify(Padding1))
                row(modify(JustifyContentEnd)) {
                    row(modify(ZenCardBg, ButtonBorderRadius, ButtonPadding)) {
                        textBlock("Reply", modify(ButtonText))
                    }.onClick(::startReply)
                }
            }
        }

        row(modify(Gap0)) {
            // indent indicator
            div(modify(ZenCardBg, Width1, BorderRadiusBottom1))

            column(modify(TalkLogClass.NestedContent, modify(Flex1))) {
                replyElement = column(modify(DisplayNone))

                childColumn = column(modify(TalkLogClass.ChildColumn)) {
                    comments.forEach { child ->
                        if (child.parentId != comment.commentId) return@forEach
                        hasChildren = true
                        addComment(child, comments)
                    }
                }
            }
        }
    }

    if (hasChildren) {
        rootElement.modify(TalkLogClass.HasNestedContent)
    }

    val view = CommentView(comment, rootElement, childColumn!!)

    renderedElements[comment.commentId] = view
}

fun ViewContext<TalkLog>.commentEditor(
    label: String,
    parentId: CommentId?,
    modifiers: ModifierSet? = null,
    onComplete: ((CommentId?) -> Unit)? = null
) {
    val text = storeOf("")

    column(modifiers) {
        textEditor(label, flow = text.flow, onValue = text::set)
        row {
            button("send", onClick = {
                if (text.now.isEmpty()) return@button
                renderScope.launch {
                    val commentId = model.sendComment(parentId, text.now)
                    if (commentId != null) {
                        text.set("")
                    }
                    onComplete?.invoke(commentId)
                }
            })
        }
    }
}