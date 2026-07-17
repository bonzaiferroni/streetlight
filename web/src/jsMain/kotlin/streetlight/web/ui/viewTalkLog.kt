package streetlight.web.ui

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.handleResponse
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.html.spacer
import koala.model.storeOf
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import org.w3c.dom.HTMLElement
import streetlight.model.data.Comment
import streetlight.model.data.CommentCreated
import streetlight.model.data.CommentUpdated
import streetlight.model.data.PostOrder
import streetlight.model.ui.TalkRoute
import streetlight.web.io.TalkLog

fun ViewScope.viewTalkLog(model: TalkLog) {
    var treeRoot: HTMLElement? = null

    column {
        filigree { heading1("Talk") }
        commentEditor("comment", "".toMarkdown()) {
            val commentId = model.createComment(null, it)
            return@commentEditor when (commentId) {
                null -> null
                else -> "".toMarkdown()
            }
        }
        row {
            dropMenu(model::setSortBy, { it.label }, model.sortByFlow)
        }

        treeRoot = column { }
    }

    parentScope.launch {

        launch {
            val comments = model.readHistory().handleResponse(toaster) ?: return@launch
            buildTree(model, treeRoot!!, comments)

            model.messageFlow.collect { message ->
                when (message) {
                    is CommentCreated -> {
                        growTree(model, treeRoot, message.comment)
                    }

                    is CommentUpdated -> {
                        updateComment(model, message)
                    }
                }
            }
        }

        launch {
            model.sortByFlow.collect {
                buildTree(model, treeRoot!!, model.comments.toList())
            }
        }
    }
}

fun ViewScope.viewTalkRoute() {
    routeBlock<TalkRoute> { route ->
        val model = TalkLog(parentScope, route.id, route.type, api)
        viewTalkLog(model)
    }
}

fun ViewScope.buildTree(model: TalkLog, treeRoot: HTMLElement, comments: List<Comment>) {
    val sortBy = model.stateNow.sortBy

    val comments = when (sortBy) {
        PostOrder.NewFirst -> comments.sortedByDescending { it.createdAt }
        PostOrder.OldFirst -> comments.sortedBy { it.createdAt }
    }
    val roots = comments.filter { it.parentId == null }

    replaceDynamicRender("comment-tree", treeRoot) {
        roots.forEach {
            val view = addCommentView(model, it, comments) ?: return@forEach
            with(view) {
                render()
            }
        }
    }
}

fun ViewScope.growTree(model: TalkLog, treeRoot: HTMLElement, comment: Comment) {
    val view = addCommentView(model, comment, emptyList()) ?: return
    when (val parentId = comment.parentId) {
        null -> {
            when (model.stateNow.sortBy) {
                PostOrder.NewFirst -> {
                    prependRender("comment", treeRoot) {
                        with (view) {
                            render()
                        }
                    }
                }
                PostOrder.OldFirst -> {
                    appendRender("comment", treeRoot) {
                        with (view) {
                            render()
                        }
                    }
                }
            }
        }
        else -> {
            val parentView = model.commentViews[parentId] ?: return
            with (parentView) {
                stageReply(view, view.isUserComment)
            }
        }
    }
}

fun ViewScope.updateComment(model: TalkLog, message: CommentUpdated) {
    val view = model.commentViews[message.commentId] ?: return

    with(view) {
        stageUpdate(message.text)
    }
}

fun ViewScope.commentEditor(
    label: String,
    initialText: Markdown,
    mod: ModifierSet? = null,
    send: suspend (Markdown) -> Markdown?
) {
    val text = storeOf(initialText)

    column(modify(Height100P, mod)) {
        textEditor(label, modify(Flex1), flow = text.flow, onValue = text::set)
        row {
            spacer(modify(Flex1))
            button("send", onClick = {
                if (text.now.value.isEmpty()) return@button
                parentScope.launch {
                    val resultText = send(text.now)
                    if (resultText != null) {
                        text.set(resultText)
                    }
                }
            })
        }
    }
}

fun ViewScope.addCommentView(
    model: TalkLog,
    comment: Comment,
    comments: List<Comment>
): CommentView? {
    if (model.commentViews.contains(comment.commentId)) return null
    val isUserComment = comment.username != null && comment.username == session.stateNow.star?.username

    val view = CommentView(comment, model, isUserComment)
    model.commentViews[comment.commentId] = view

    comments.forEach {
        if (it.parentId != comment.commentId) return@forEach
        val reply = addCommentView(model, it, comments) ?: return@forEach
        view.addReply(reply)
    }
    return view
}

fun TagScope.zenButton(mod: ModifierSet? = null, block: DIV.() -> Unit) =
    row(modify(mod, ZenBg, ButtonBorderRadius, ButtonPadding)) {
        block()
    }