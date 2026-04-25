package streetlight.web.ui

import kampfire.api.StringId
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.model.storeOf
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import streetlight.model.data.Comment
import streetlight.model.data.CommentId
import streetlight.model.data.TalkHistory
import streetlight.model.data.SpaceType
import streetlight.model.data.TalkComment
import streetlight.web.TalkRoute
import streetlight.web.io.TalkLog
import streetlight.web.model.Streetlight

fun ViewContext<TalkLog>.viewTalkLog() {
    var treeRoot: HTMLElement? = null

    column {
        filigree { heading1("Talk") }
        commentEditor("comment", null)
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
        model.commentViews[parentId]?.also { it.body.modify(TalkLogClass.HasNestedContent) }?.childColumn
    } ?: treeRoot

    appendRender(container) {
        addComment(comment, emptyList())
    }
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

fun DOMContext.softButton(text: String) = row(modify(ZenCardBg, ButtonBorderRadius, ButtonPadding)) {
    textBlock(text, modify(ButtonText))
}