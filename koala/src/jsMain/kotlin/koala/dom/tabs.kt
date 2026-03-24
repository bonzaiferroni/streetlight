package koala.dom

import koala.css.*
import koala.html.Id
import koala.html.TabClass
import kotlinx.html.js.p
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

fun RenderContext.tabs(
    id: Id? = null,
    modifiers: ModifierSet? = null,
    content: TabScope.() -> Unit,
): HTMLDivElement {
    val scope = TabScope()
    scope.content()
    val tabPanelElements = Array<HTMLElement?>(3) { null }
    val renders = Array<RenderContext?>(3) { null }
    val root = column(id, modify(TabClass.tabs, modifiers)) {
        row(modify(TabClass.header)) {
            scope.tabs.forEachIndexed { index, tab ->
                val button = p {
                    setModifiers(TabClass.button)
                    attributes["data-tab"] = index.toString()
                    +tab.label
                }
                fun createTab(): RenderContext {
                    val element = tabPanelElements.getOrNull(index) ?: error("tab not found: $index")
                    val context = createRender(element, renderScope, tab.content).context
                    renders[index] = context
                    return context
                }
                fun selectTab(event: Event) {
                    val context = renders.getOrNull(index) ?: createTab()
                }
                button.addEventListener("select-tab", ::selectTab)
            }
        }
        box(modify(TabClass.viewport)) {
            scope.tabs.forEachIndexed { index, tab ->
                val element = box(modify(TabClass.panel))
                tabPanelElements[index] = element
            }
        }
    }

    initTabs(root)

    return root
}

fun TabScope.tab(
    label: String,
    content: RenderContext.() -> Unit
) {
    add(label, content)
}

class TabScope {
    private val _tabs: MutableList<Tab> = mutableListOf()
    val tabs: List<Tab> = _tabs

    fun add(label: String, content: RenderContext.() -> Unit) {
        _tabs.add(Tab(
            label = label,
            content = content
        ))
    }
}

data class Tab(
    val label: String,
    val id: Id = Id(label),
    val content: RenderContext.() -> Unit
)