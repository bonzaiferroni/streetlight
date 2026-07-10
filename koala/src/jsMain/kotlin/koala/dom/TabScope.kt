package koala.dom

import koala.css.modify
import koala.html.Id
import koala.html.TabClass
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

fun <T: TagScope> TabScope<T>.tab(
    label: String,
    colorScheme: String? = null,
    content: T.() -> Unit
) {
    add(label, colorScheme, content)
}

class TabScope<T: TagScope>(
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

    fun add(label: String, colorScheme: String?, content: T.() -> Unit) {
        _tabs.add(Tab(
            label = label,
            colorScheme = colorScheme,
            content = content
        ))
    }

    @Suppress("UNCHECKED_CAST")
    internal fun createTab(receiver: T, index: Int) {
        val element = elementCache.getOrNull(index) ?: error("tab not found: $index")
        val tab = tabs[index]
        when (receiver) {
            is AppScope -> {
                val tab = tab as Tab<AppScope>
                element.replaceDynamicRender(receiver.app, receiver.parentScope, tab.content)
            }
            is TagScope -> {
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

    internal fun renderTab(receiver: T, index: Int) {
        val element = elementCache.getOrNull(index) ?: error("tabs not built")
        if (element.childElementCount == 0) {
            createTab(receiver, index)
        }
    }
}

data class Tab<T: TagScope>(
    val label: String,
    val id: Id = Id(label),
    val colorScheme: String?,
    val content: T.() -> Unit
)