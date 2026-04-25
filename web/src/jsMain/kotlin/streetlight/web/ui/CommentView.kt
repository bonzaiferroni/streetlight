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
    val isUserComment: Boolean,
) {
    private var _rootBlock: HTMLElement? = null
    private var _childBlock: HTMLElement? = null
    private var _replyBlock: HTMLElement? = null
    private var _bodyBlock: HTMLElement? = null
    private var _editBlock: HTMLElement? = null
    private var _hasChildren: Boolean = false

    val rootBlock get() = _rootBlock ?: error("body not found")
    val childBlock get() = _childBlock ?: error("child column not found")
    val replyBlock get() = _replyBlock ?: error("reply element not found")
    val bodyBlock get() = _bodyBlock ?: error("body block not found")
    val editBlock get() = _editBlock ?: error("edit block not found")
    val hasChildren get() = _hasChildren

    var isEditing
        get() = bodyBlock.isModified(CommentClass.IsEditing)
        set(value: Boolean) {
            when (value) {
                true -> bodyBlock.modify(CommentClass.IsEditing)
                false -> bodyBlock.unmodify(CommentClass.IsEditing)
            }
        }

    var isReplying
        get() = replyBlock.isModified(CommentClass.IsReplying)
        set(value: Boolean) {
            when (value) {
                true -> replyBlock.modify(CommentClass.IsReplying)
                false -> replyBlock.unmodify(CommentClass.IsReplying)
            }
        }

    fun ViewContext<TalkLog>.startReply() {
        isReplying = !isReplying
        if (isReplying) {
            rootBlock.modify(CommentClass.HasNestedContent)
            if (replyBlock.hasChildNodes()) return
            replaceRender(replyBlock) {
                commentEditor("reply", comment.commentId, "", false, modify(AutoMagic, SlideLeft)) { commentId ->
                    if (commentId != null) {
                        replyBlock.modify(DisplayNone)
                    }
                }
            }
        } else {
            if (!hasChildren) {
                rootBlock.unmodify(CommentClass.HasNestedContent)
            }
        }
    }

    fun ViewContext<TalkLog>.toggleEdit() {
        isEditing = !isEditing
        if (isEditing) {
            if (editBlock.hasChildNodes()) return
            replaceRender(editBlock) {
                commentEditor("edit", comment.parentId, comment.text, true)
            }
        }
    }

    fun ViewContext<TalkLog>.render(comments: List<Comment>) {
        _rootBlock = column(modify(CommentClass.Comment, Gap0)) {
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
                    _bodyBlock = box(modify(CommentClass.Body)) {
                        _editBlock = div(modify(CommentClass.Editor))
                        markdown(comment.text, modify(CommentClass.Content, Padding1))
                    }
                    row(modify(AlignItemsCenter)) {
                        if (isUserComment) {
                            zenButton("edit", "cancel").onClick {
                                toggleEdit()
                            }
                        }
                        spacer(modify(Flex1))
                        zenButton("reply", "cancel").onClick {
                            startReply()
                        }
                    }
                }
            }

            row(modify(Gap0)) {
                // indent indicator
                div(modify(ZenCardBg, Width1, BorderRadiusBottom1))

                column(modify(CommentClass.NestedContent, modify(Flex1))) {
                    _replyBlock = div(modify(CommentClass.Reply))

                    _childBlock = column(modify(CommentClass.ChildColumn)) {
                        comments.forEach { child ->
                            if (child.parentId != comment.commentId) return@forEach
                            _hasChildren = true
                            addComment(child, comments)
                        }
                    }
                }
            }
        }

        if (hasChildren) {
            rootBlock.modify(CommentClass.HasNestedContent)
        }
    }
}

private val controlMod = modify(BorderRadius1, FlexDirectionRow, AlignItemsCenter)


