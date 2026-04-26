package streetlight.web.ui

import io.ktor.util.collections.setValue
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.html.spacer
import koala.model.storeOf
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import streetlight.model.data.Comment
import streetlight.model.data.CommentId
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
            addComment(it, comments)
        }
    }
}

fun ViewContext<TalkLog>.growTree(treeRoot: HTMLElement, comment: Comment) {
    when (val parentId = comment.parentId) {
        null -> {
            when (model.stateNow.sortBy) {
                SortBy.New -> {
                    prependRender(treeRoot) {
                        addComment(comment, emptyList())
                    }
                }
                SortBy.Old -> {
                    appendRender(treeRoot) {
                        addComment(comment, emptyList())
                    }
                }
            }
        }
        else -> {
            val parentView = model.commentViews[parentId] ?: return // incorrect, could be nested reply
            val isUserReply = comment.username != null && comment.username == model.app.gate.stateNow.star?.username
            with (parentView) {
                stageReply(comment, isUserReply)
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

fun ViewContext<TalkLog>.addComment(comment: Comment, comments: List<Comment>) {
    if (model.commentViews.contains(comment.commentId)) return
    val isUserComment = model.app.gate.stateNow.star?.username == comment.username && comment.username != null

    val view = CommentView(comment, isUserComment)
    with(view) {
        render(comments)
    }

    model.commentViews[comment.commentId] = view
}

fun DOMContext.zenButton(modifiers: ModifierSet? = null, block: DIV.() -> Unit) =
    row(modify(modifiers, ZenCardBg, ButtonBorderRadius, ButtonPadding)) {
        block()
    }