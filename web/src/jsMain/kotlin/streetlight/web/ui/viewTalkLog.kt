package streetlight.web.ui

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
import streetlight.model.data.TalkHistory
import streetlight.model.data.CommentCreated
import streetlight.model.data.CommentUpdated
import streetlight.web.TalkRoute
import streetlight.web.io.SortBy
import streetlight.web.io.TalkLog
import streetlight.web.model.Streetlight

fun ViewContext<TalkLog>.viewTalkLog() {
    var treeRoot: HTMLElement? = null

    column {
        filigree { heading1("Talk") }
        commentEditor("comment", "") {
            val commentId = model.createComment(null, it)
            return@commentEditor when (commentId) {
                null -> null
                else -> ""
            }
        }
        row {
            dropMenu(model::setSortBy, { it.label }, model.sortByFlow)
        }

        treeRoot = column { }

        appFooter("")
    }

    renderScope.launch {
        launch {
            model.messageFlow.collect { message ->
                when (message) {
                    is TalkHistory -> {
                        buildTree(treeRoot!!, message.comments)
                    }

                    is CommentCreated -> {
                        growTree(treeRoot!!, message.comment)
                    }

                    is CommentUpdated -> {
                        updateComment(message)
                    }
                }
            }
        }

        launch {
            model.sortByFlow.collect {
                buildTree(treeRoot!!, model.comments.toList())
            }
        }
    }
}

fun ViewContext<Streetlight>.viewTalkRoute() {
    routeBlock<TalkRoute> { route ->
        val model = TalkLog(renderScope, model, route.stringId, route.type)
        viewContextOf(model) {
            viewTalkLog()
        }
    }
}

fun ViewContext<TalkLog>.buildTree(treeRoot: HTMLElement, comments: List<Comment>) {
    val sortBy = model.stateNow.sortBy

    val comments = when (sortBy) {
        SortBy.New -> comments.sortedByDescending { it.createdAt }
        SortBy.Old -> comments.sortedBy { it.createdAt }
    }
    val roots = comments.filter { it.parentId == null }

    replaceRender(treeRoot) {
        roots.forEach {
            val view = addCommentView(it, comments) ?: return@forEach
            with(view) {
                render()
            }
        }
    }
}

fun ViewContext<TalkLog>.growTree(treeRoot: HTMLElement, comment: Comment) {
    when (val parentId = comment.parentId) {
        null -> {
            when (model.stateNow.sortBy) {
                SortBy.New -> {
                    prependRender(treeRoot) {
                        addCommentView(comment, emptyList())
                    }
                }
                SortBy.Old -> {
                    appendRender(treeRoot) {
                        addCommentView(comment, emptyList())
                    }
                }
            }
        }
        else -> {
            val parentView = model.commentViews[parentId] ?: return
            val isUserComment = comment.username != null && comment.username == model.app.gate.stateNow.star?.username
            val view = CommentView(comment, isUserComment)
            with (parentView) {
                stageReply(view, isUserComment)
            }
        }
    }
}

fun ViewContext<TalkLog>.updateComment(message: CommentUpdated) {
    val view = model.commentViews[message.commentId] ?: return

    with(view) {
        stageUpdate(message.text)
    }
}

fun ViewContext<TalkLog>.commentEditor(
    label: String,
    initialText: String,
    modifiers: ModifierSet? = null,
    send: suspend (String) -> String?
) {
    val text = storeOf(initialText)

    column(modify(Height100P, modifiers)) {
        textEditor(label, modify(Flex1), flow = text.flow, onValue = text::set)
        row {
            spacer(modify(Flex1))
            button("send", onClick = {
                if (text.now.isEmpty()) return@button
                renderScope.launch {
                    val resultText = send(text.now)
                    if (resultText != null) {
                        text.set(resultText)
                    }
                }
            })
        }
    }
}

fun ViewContext<TalkLog>.addCommentView(comment: Comment, comments: List<Comment>): CommentView? {
    if (model.commentViews.contains(comment.commentId)) return null
    val isUserComment = comment.username != null && comment.username != gate.stateNow.star?.username

    val view = CommentView(comment, isUserComment)
    model.commentViews[comment.commentId] = view

    comments.forEach {
        if (it.parentId != comment.commentId) return@forEach
        val reply = addCommentView(it, comments) ?: return@forEach
        view.addReply(reply)
    }
    return view
}

fun DOMContext.zenButton(modifiers: ModifierSet? = null, block: DIV.() -> Unit) =
    row(modify(modifiers, ZenCardBg, ButtonBorderRadius, ButtonPadding)) {
        block()
    }