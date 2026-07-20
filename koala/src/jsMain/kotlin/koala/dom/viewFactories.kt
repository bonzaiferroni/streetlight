package koala.dom

import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.dom.prepend
import org.w3c.dom.HTMLElement

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

fun ViewScope.mountChildView(
    id: Id,
    ancestor: HTMLElement? = null,
    block: ViewScope.() -> Unit
) = mountChildView(id.identifier, ((ancestor ?: document.body!!).querySelector(id) ?: error("element not found: $id")), block)

fun ViewScope.appendChildView(
    name: String,
    element: HTMLElement,
    block: ViewScope.() -> Unit
): View {
    lateinit var view: View
    element.append {
        view = View(this@append, contentScope, name, app, element, this@appendChildView)
        view.applyView(InsertEdge.Tail, block)
    }
    return view
}

fun ViewScope.prependChildView(
    name: String,
    element: HTMLElement,
    block: ViewScope.() -> Unit
): View {
    lateinit var view: View
    element.prepend {
        view = View(this@prepend, contentScope, name, app, element, this@prependChildView)
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