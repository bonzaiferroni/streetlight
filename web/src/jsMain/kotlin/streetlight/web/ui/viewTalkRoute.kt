package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.model.storeOf
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import streetlight.model.data.Comment
import streetlight.model.data.CommentId
import streetlight.model.data.TalkHistory
import streetlight.model.data.TalkComment
import streetlight.web.TalkRoute
import streetlight.web.io.TalkLog
import streetlight.web.model.Streetlight

fun ViewContext<TalkLog>.viewTalkLog() {
    var treeRoot: HTMLElement? = null

    column {
        filigree { heading1("Talk") }
        commentEditor("comment", null, "", false)
        treeRoot = column { }
    }

    renderScope.launch {
        model.messageFlow.collect { message ->
            when (message) {
                is TalkHistory -> {
                    buildTree(treeRoot!!, message)
                }
                is TalkComment -> {
                    growTree(treeRoot!!, message.comment)
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

fun ViewContext<TalkLog>.commentEditor(
    label: String,
    parentId: CommentId?,
    initialText: String,
    isEdit: Boolean,
    modifiers: ModifierSet? = null,
    onComplete: ((CommentId?) -> Unit)? = null
) {
    val text = storeOf(initialText)

    column(modifiers) {
        textEditor(label, flow = text.flow, onValue = text::set)
        row {
            button("send", onClick = {
                if (text.now.isEmpty()) return@button
                renderScope.launch {
                    when (isEdit) {
                        true -> {

                        }
                        else -> {
                            val commentId = model.sendComment(parentId, text.now)
                            if (commentId != null) {
                                text.set("")
                            }
                            onComplete?.invoke(commentId)
                        }
                    }
                }
            })
        }
    }
}

fun ViewContext<TalkLog>.addComment(comment: Comment, comments: List<Comment>) {
    if (model.commentViews.contains(comment.commentId)) return
    val isUserComment = model.app.gate.stateNow.star?.username == comment.username

    val view = CommentView(comment, isUserComment)
    with (view) {
        render(comments)
    }

    model.commentViews[comment.commentId] = view
}

fun DOMContext.zenButton(text: String, toggleText: String? = null): HTMLElement {
    var isToggled = false

    var textElement: HTMLParagraphElement? = null

    val element = row(modify(ZenCardBg, ButtonBorderRadius, ButtonPadding)) {
        textElement = textBlock(text, modify(ButtonText))
    }

    if (toggleText != null && textElement != null) {
        element.onClick {
            isToggled = !isToggled
            if (isToggled) {
                textElement.textContent = toggleText
            } else {
                textElement.textContent = text
            }
        }
    }

    return element
}