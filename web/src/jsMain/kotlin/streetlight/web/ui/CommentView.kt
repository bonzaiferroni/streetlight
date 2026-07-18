package streetlight.web.ui

import kabinet.utils.toAgoFormat
import kampfire.api.Markdown
import kampfire.api.toMarkdown
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
import streetlight.model.ui.StarRoute
import streetlight.web.io.TalkLog

class CommentView(
    comment: Comment,
    val model: TalkLog,
    val isUserComment: Boolean,
) {
    var comment = comment
        private set
    var isRendered = false
        private set
    var hasReplies = false
        private set

    private var _rootBlock: HTMLElement? = null
    private var _repliesBlock: HTMLElement? = null
    private var _replyBlock: HTMLElement? = null
    private var _bodyBlock: HTMLElement? = null
    private var _editBlock: HTMLElement? = null
    private var _contentBlock: HTMLElement? = null
    private var _editButtonText: HTMLParagraphElement? = null
    private var _replyButtonText: HTMLParagraphElement? = null
    private var _showUpdateButton: HTMLElement? = null

    val rootBlock get() = _rootBlock ?: error("body not found")
    val repliesBlock get() = _repliesBlock ?: error("child column not found")
    val replyBlock get() = _replyBlock ?: error("reply element not found")
    val bodyBlock get() = _bodyBlock ?: error("body block not found")
    val editBlock get() = _editBlock ?: error("edit block not found")
    val contentBlock get() = _contentBlock ?: error("content block not found")
    val editButtonText get() = _editButtonText ?: error("edit button text not found")
    val replyButtonText get() = _replyButtonText ?: error("reply button text not")
    val showUpdateButton get() = _showUpdateButton ?: error("show update button not found")

    private var stagedUpdate: Markdown? = null
    private val stagedReplies = mutableListOf<CommentView>()
    private val replies = mutableListOf<CommentView>()

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

    fun addReply(reply: CommentView) {
        hasReplies = true
        replies.add(reply)
    }

    fun ViewScope.replyAction() {
        if (stagedReplies.isNotEmpty()) {
            showStagedReplies()
        } else {
            startReply()
        }
    }

    fun ViewScope.startReply() {
        isReplying = !isReplying
        if (isReplying) {
            rootBlock.modify(CommentClass.HasNestedContent)
            if (replyBlock.hasChildNodes()) return
            mountChildView("comment-reply", replyBlock) {
                commentEditor("reply", "".toMarkdown(), modify(AutoMagic, SlideLeft)) { text ->
                    val commentId = model.createComment(comment.commentId, text)

                    return@commentEditor when (commentId) {
                        null -> null
                        else -> {
                            replyBlock.clear()
                            isReplying = false
                            "".toMarkdown()
                        }
                    }
                }
            }
        } else {
            if (!hasReplies) {
                rootBlock.unmodify(CommentClass.HasNestedContent)
            }
        }
    }

    fun ViewScope.toggleEdit() {
        isEditing = !isEditing
        if (isEditing) {
            editBlock.unmodify(DisplayNone)
            if (editBlock.hasChildNodes()) return
            mountChildView("comment-edit", editBlock) {
                commentEditor("edit", comment.text) { text ->
                    val isSuccess = model.updateComment(comment.commentId, text)
                    return@commentEditor when (isSuccess) {
                        true -> {
                            isEditing = false
                            editBlock.modify(DisplayNone)
                            text
                        }
                        else -> {
                            text
                        }
                    }
                }
            }
        } else {
            editBlock.modify(DisplayNone)
        }
    }

    fun ViewScope.stageReply(comment: CommentView, isUserReply: Boolean) {
        if (isUserReply) {
            renderStagedReplies(listOf(comment))
        } else {
            stagedReplies.add(comment)
            replyButtonText.textContent = "show ${stagedReplies.size} new replies"
        }
    }

    fun ViewScope.showStagedReplies() {
        val replies = stagedReplies.toList()
        stagedReplies.clear()
        renderStagedReplies(replies)
        replyButtonText.textContent = "reply"
        rootBlock.modify(CommentClass.HasNestedContent)
    }

    private fun ViewScope.renderStagedReplies(replies: List<CommentView>) {
        prependChildView("comment-replies", repliesBlock) {
            replies.forEach { reply ->
                with (reply) {
                    render()
                }
            }
        }
    }

    fun ViewScope.stageUpdate(text: Markdown) {
        if (isUserComment) {
            updateTextContent(text)
        } else {
            stagedUpdate = text
            showUpdateButton.unmodify(DisplayNone)
        }
    }

    fun ViewScope.showStagedUpdate() {
        val text = stagedUpdate ?: error("staged update not found")
        showUpdateButton.modify(DisplayNone)
        updateTextContent(text)
    }

    private fun ViewScope.updateTextContent(text: Markdown) {
        comment = comment.copy(text = text)

        mountChildView("comment-text", contentBlock) {
            markdown(text)
        }
    }

    fun ViewScope.render() {
        if (isRendered) return
        isRendered = true

        _rootBlock = column(modify(CommentClass.Root, Gap0)) {
            card(modify(ZenBg, Gap0, Padding0, OverflowClip, AutoMagic)) {
                row(modify(AlignItemsCenter, modify(ZenBg, Padding1))) {
                    navigationIfNotNull(comment.username?.let { StarRoute(it) }) {
                        row(modify(AlignItemsCenter)) {
                            image(comment.thumb, modify(Aspect1, Height6, BorderRadius50P))
                            column(modify(Gap0)) {
                                textBlock(comment.username?.value ?: "[Former Guest]")
                                textBlock(comment.createdAt.toAgoFormat(), modify(OpacityHigh, TextSmall))
                            }
                        }
                    }
                    spacer(modify(Flex1))
                    icon(SvgFile.EyeMinus, modify(Height5, OpacityHigh)).onClickElement {
                        val isHidden = rootBlock.toggle(Hide)
                        val svg = when (isHidden) {
                            true -> SvgFile.EyePlus
                            else -> SvgFile.EyeMinus
                        }
                        it.setStyle(Property.MaskUrl.to(svg))
                    }
                }
                column(modify(Padding1, CommentClass.InnerCard)) {
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
                            replyAction()
                        }
                    }
                }
            }

            row(modify(Gap0, CommentClass.AfterCard)) {
                // indent indicator
                div(modify(ZenBg, Width1, BorderRadiusBottom1))

                column(modify(CommentClass.NestedContent, modify(Flex1))) {
                    _replyBlock = div(modify(CommentClass.Reply))

                    _repliesBlock = column(modify(CommentClass.ChildColumn)) {
                        replies.forEach { reply ->
                            with (reply) {
                                render()
                            }
                        }
                    }
                }
            }
        }

        if (hasReplies) {
            rootBlock.modify(CommentClass.HasNestedContent)
        }
    }
}


