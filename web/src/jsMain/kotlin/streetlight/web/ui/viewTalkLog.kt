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
        treeRoot = column { }

        appFooter("")
    }

    renderScope.launch {
        model.messageFlow.collect { message ->
            when (message) {
                is TalkHistory -> {
                    buildTree(treeRoot!!, message)
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
}

fun ViewContext<Streetlight>.viewTalkRoute() {
    routeBlock<TalkRoute> { route ->
        val model = TalkLog(renderScope, model, route.stringId, route.type)
        viewContextOf(model) {
            viewTalkLog()
        }
    }
}

fun ViewContext<TalkLog>.buildTree(treeRoot: HTMLElement, history: TalkHistory) {
    val comments = history.comments.sortedBy { it.createdAt }
    val roots = comments.filter { it.parentId == null }

    replaceRender(treeRoot) {
        roots.forEach {
            addComment(it, comments)
        }
    }
}

fun ViewContext<TalkLog>.growTree(treeRoot: HTMLElement, comment: Comment) {
    val container = comment.parentId?.let { parentId ->
        model.commentViews[parentId]?.also { it.rootBlock.modify(CommentClass.HasNestedContent) }?.childBlock
    } ?: treeRoot

    appendRender(container) {
        addComment(comment, emptyList())
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