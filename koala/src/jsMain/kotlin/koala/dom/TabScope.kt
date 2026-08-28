package koala.dom

import koala.css.modify
import koala.html.Id
import koala.html.TabClass
import kotlinx.html.dom.append
import web.html.HTMLElement

class TabScope(
    maxTabCount: Int = 5,
    private val content: TabScope.() -> Unit,
) {
    private val _tabs: MutableList<Tab> = mutableListOf()
    val tabs: List<Tab> = _tabs

    private var elementCache = Array<HTMLElement?>(maxTabCount) { null }
    private var viewport: HTMLElement? = null

    init {
        content()
    }

    fun tab(
        label: String,
        colorScheme: String? = null,
        content: ViewScope.() -> Unit
    ) {
        _tabs.add(Tab(
            label = label,
            colorScheme = colorScheme,
            content = content
        ))
    }

    @Suppress("UNCHECKED_CAST")
    internal fun createTab(receiver: ViewScope, index: Int) {
        val element = elementCache.getOrNull(index) ?: error("tab not found: $index")
        val tab = tabs[index]
        receiver.mountChildView("tab", element, tab.content)
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
}

data class Tab(
    val label: String,
    val id: Id = Id(label),
    val colorScheme: String?,
    val content: ViewScope.() -> Unit
)