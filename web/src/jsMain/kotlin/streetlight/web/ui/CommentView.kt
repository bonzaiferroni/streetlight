package streetlight.web.ui

import kabinet.utils.toAgoFormat
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.image
import koala.html.markdown
import koala.html.navigationIfNotNull
import koala.html.spacer
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import streetlight.model.data.Comment
import streetlight.web.StarRoute
import streetlight.web.io.TalkLog

class CommentView(
    val comment: Comment,
    val body: HTMLElement,
    val childColumn: HTMLElement,
) {

}

fun ViewContext<TalkLog>.addComment(comment: Comment, comments: List<Comment>) {
    if (model.commentViews.contains(comment.commentId)) return
    val isUserComment = model.app.gate.stateNow.star?.username == comment.username

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

    fun startEdit() {

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
                row(modify(AlignItemsCenter)) {
                    if (isUserComment) {
                        softButton("edit")
                    }
                    spacer(modify(Flex1))
                    softButton("reply").onClick(::startReply)
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

    model.commentViews[comment.commentId] = view
}
