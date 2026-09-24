package koala.dom

import koala.html.Id
import kotlinx.coroutines.CoroutineScope
import web.dom.document
import web.html.HTMLElement

/** Replaces this element's children with a root view built by [block], in [parentScope]. */
fun HTMLElement.mountRootView(
    name: String,
    parentScope: CoroutineScope,
    app: AppContainer,
    block: ViewScope.() -> Unit
): View {
    clear()
    lateinit var view: View
    append {
        view = View(this@append, parentScope, name, app, this@mountRootView, null)
        view.applyView(InsertEdge.Tail, block)
    }
    return view
}

/**
 * Replaces the children of the element with [id] with a root view built by [block]. Throws when there is no such
 * element.
 */
fun mountRootView(
    id: Id,
    parentScope: CoroutineScope,
    app: AppContainer,
    ancestor: HTMLElement? = null,
    block: ViewScope.() -> Unit
): View {
    val element = (ancestor ?: document.body).querySelector(id) ?: error("element not found: $id")
    return element.mountRootView(id.selector, parentScope, app, block)
}

/**
 * Replaces [mount]'s children with a child view of this one, built by [block].
 *
 * A child view's coroutines end with this view's content. A failure while building leaves an error message in
 * place of the view.
 */
fun ViewScope.mountChildView(
    name: String,
    mount: HTMLElement,
    block: ViewScope.() -> Unit,
): View {
    mount.clear()
    lateinit var view: View
    mount.append {
        view = View(this@append, contentScope, name, app, mount, this@mountChildView)
        view.applyView(InsertEdge.Tail, block)
    }
    return view
}

/** Mounts a child view of this one, built by [block], into the element with [id]. */
fun ViewScope.mountChildView(
    id: Id,
    ancestor: HTMLElement? = null,
    block: ViewScope.() -> Unit
) = mountChildView(id.identifier, ((ancestor ?: document.body).querySelector(id) ?: error("element not found: $id")), block)

/** Appends a child view of [viewScope], built by [block], after this element's children. */
fun HTMLElement.appendChildView(
    name: String,
    viewScope: ViewScope,
    block: ViewScope.() -> Unit
): View {
    lateinit var view: View
    append {
        view = View(this@append, viewScope.contentScope, name, viewScope.app, this@appendChildView, viewScope)
        view.applyView(InsertEdge.Tail, block)
    }
    return view
}

/** Prepends a child view of [viewScope], built by [block], before this element's children. */
fun HTMLElement.prependChildView(
    name: String,
    viewScope: ViewScope,
    block: ViewScope.() -> Unit
): View {
    lateinit var view: View
    prepend {
        view = View(this@prepend, viewScope.contentScope, name, viewScope.app, this@prependChildView, viewScope)
        view.applyView(InsertEdge.Head, block)
    }
    return view
}

internal enum class InsertEdge { Head, Tail }

private fun View.applyView(edge: InsertEdge, block: ViewScope.() -> Unit) {
    val countBefore = mount.childElementCount
    try {
        block()
    } catch (e: Throwable) {
        val added = mount.childElementCount - countBefore
        repeat(added) {
            when (edge) {
                InsertEdge.Tail -> mount.lastElementChild?.remove()
                InsertEdge.Head -> mount.firstElementChild?.remove()
            }
        }
        this.dispose()
        when (edge) {
            InsertEdge.Tail -> mount.append { textBlock("Something went wrong.") }
            InsertEdge.Head -> mount.prepend { textBlock("Something went wrong.") }
        }
        throw e
    }
}