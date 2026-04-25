package streetlight.web.ui

import kabinet.utils.toAgoFormat
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.image
import koala.html.markdown
import koala.html.navigationIfNotNull
import koala.html.spacer
import kotlinx.dom.clear
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import streetlight.model.data.Comment
import streetlight.web.StarRoute
import streetlight.web.io.TalkLog

class CommentView(
    comment: Comment,
    val isUserComment: Boolean,
) {
    var comment = comment
        private set

    private var _rootBlock: HTMLElement? = null
    private var _childBlock: HTMLElement? = null
    private var _replyBlock: HTMLElement? = null
    private var _bodyBlock: HTMLElement? = null
    private var _editBlock: HTMLElement? = null
    private var _contentBlock: HTMLElement? = null
    private var _editButtonText: HTMLParagraphElement? = null
    private var _replyButtonText: HTMLParagraphElement? = null
    private var _showUpdateButton: HTMLElement? = null
    private var _hasChildren: Boolean = false

    val rootBlock get() = _rootBlock ?: error("body not found")
    val childBlock get() = _childBlock ?: error("child column not found")
    val replyBlock get() = _replyBlock ?: error("reply element not found")
    val bodyBlock get() = _bodyBlock ?: error("body block not found")
    val editBlock get() = _editBlock ?: error("edit block not found")
    val contentBlock get() = _contentBlock ?: error("content block not found")
    val editButtonText get() = _editButtonText ?: error("edit button text not found")
    val replyButtonText get() = _replyButtonText ?: error("reply button text not")
    val showUpdateButton get() = _showUpdateButton ?: error("show update button not found")
    val hasChildren get() = _hasChildren

    private var stagedUpdate: String? = null
    private val stagedReplies = mutableListOf<Comment>()

    var isEditing
        get() = bodyBlock.isModified(CommentClass.IsEditing)
        set(value: Boolean) {
            when (value) {
                true -> {
                    bodyBlock.modify(CommentClass.IsEditing)
                    editButtonText.textContent = "cancel"
                }
                false -> {
                    bodyBlock.unmodify(CommentClass.IsEditing)
                    editButtonText.textContent = "edit"
                }
            }
        }

    var isReplying
        get() = replyBlock.isModified(CommentClass.IsReplying)
        set(value: Boolean) {
            when (value) {
                true -> {
                    replyBlock.modify(CommentClass.IsReplying)
                    replyButtonText.textContent = "cancel"
                }
                false -> {
                    replyBlock.unmodify(CommentClass.IsReplying)
                    replyButtonText.textContent = "reply"
                }
            }
        }

    fun ViewContext<TalkLog>.replyAction() {

    }

    fun ViewContext<TalkLog>.startReply() {
        isReplying = !isReplying
        if (isReplying) {
            rootBlock.modify(CommentClass.HasNestedContent)
            if (replyBlock.hasChildNodes()) return
            replaceRender(replyBlock) {
                commentEditor("reply", "", modify(AutoMagic, SlideLeft)) { text ->
                    val commentId = model.createComment(comment.commentId, text)
                    console.log(commentId)

                    return@commentEditor when (commentId) {
                        null -> null
                        else -> {
                            replyBlock.clear()
                            isReplying = false
                            ""
                        }
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
                commentEditor("edit", comment.text) { text ->
                    val isSuccess = model.updateComment(comment.commentId, text)
                    return@commentEditor when (isSuccess) {
                        true -> {
                            isEditing = false
                            text
                        }
                        else -> {
                            text
                        }
                    }
                }
            }
        }
    }

    fun ViewContext<TalkLog>.stageUpdate(text: String) {
        if (isUserComment) {
            updateTextContent(text)
        } else {
            stagedUpdate = text
            showUpdateButton.unmodify(DisplayNone)
        }
    }

    fun ViewContext<TalkLog>.showStagedUpdate() {
        val text = stagedUpdate ?: error("staged update not found")
        showUpdateButton.modify(DisplayNone)
        updateTextContent(text)
    }

    private fun ViewContext<TalkLog>.updateTextContent(text: String) {
        comment = comment.copy(text = text)

        replaceRender(contentBlock) {
            markdown(text)
        }
    }

    fun ViewContext<TalkLog>.stageReply(comment: Comment) {

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
                            icon(SvgFile.MessagePlus, modify(Height4))
                        }
                    }
                }
                column(modify(Padding1)) {
                    _bodyBlock = box(modify(CommentClass.Body)) {
                        _editBlock = div(modify(CommentClass.Editor, Height100P))
                        _contentBlock = div(modify(CommentClass.Content, Padding1)) {
                            markdown(comment.text)
                        }
                    }
                    row(modify(AlignItemsCenter)) {
                        if (isUserComment) {
                            zenButton {
                                _editButtonText = textBlock("edit", modify(ButtonText))
                            }.onClick {
                                toggleEdit()
                            }
                        } else {
                            _showUpdateButton = zenButton(modify(DisplayNone)) {
                                textBlock("show update", modify(ButtonText))
                            }.onClick {
                                showStagedUpdate()
                            }
                        }
                        spacer(modify(Flex1))
                        zenButton {
                            _replyButtonText = textBlock("reply", modify(ButtonText))
                        }.onClick {
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


