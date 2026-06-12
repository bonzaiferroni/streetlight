package koala.dom

import koala.css.modify
import koala.html.Id
import koala.html.TabClass
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

fun <T: AppendScope> TabScope<T>.tab(
    label: String,
    content: T.() -> Unit
) {
    add(label, content)
}

class TabScope<T: AppendScope>(
    maxTabCount: Int = 5,
    private val content: TabScope<T>.() -> Unit,
) {
    private val _tabs: MutableList<Tab<T>> = mutableListOf()
    val tabs: List<Tab<T>> = _tabs

    private var elementCache = Array<HTMLElement?>(maxTabCount) { null }
    private var viewport: HTMLElement? = null

    init {
        content()
    }

    fun add(label: String, content: T.() -> Unit) {
        _tabs.add(Tab(
            label = label,
            content = content
        ))
    }

    @Suppress("UNCHECKED_CAST")
    internal fun createTab(receiver: T, index: Int) {
        val element = elementCache.getOrNull(index) ?: error("tab not found: $index")
        val tab = tabs[index]
        when (receiver) {
            is RenderScope -> {
                val tab = tab as Tab<RenderScope>
                element.replaceRender(receiver.app, receiver.parentScope, tab.content)
            }
            is AppendScope -> {
                element.append {
                    tab.content(receiver)
                }
            }
        }
    }

    internal fun build(viewport: HTMLElement) {
        this.viewport = viewport
        viewport.append {
            tabs.forEachIndexed { index, _ ->
                val element = box(modify(TabClass.panel))
                elementCache[index] = element
            }
        }
    }

    internal fun selectTab(receiver: T, index: Int) {
        val element = elementCache.getOrNull(index) ?: error("tabs not built")
        if (element.childElementCount == 0) {
            createTab(receiver, index)
        }
    }
}

data class Tab<T: AppendScope>(
    val label: String,
    val id: Id = Id(label),
    val content: T.() -> Unit
)