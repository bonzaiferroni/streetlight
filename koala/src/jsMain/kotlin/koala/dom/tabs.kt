package koala.dom

import koala.css.*
import koala.html.Id
import koala.html.TabClass
import kotlinx.html.DIV
import kotlinx.html.dom.append
import kotlinx.html.js.p
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

fun RenderContext.tabs(
    id: Id,
    modifiers: ModifierSet? = null,
    content: TabScope.() -> Unit,
): HTMLDivElement {
    val scope = TabScope()
    scope.content()
    val tabPanelElements = mutableListOf<HTMLElement?>()
    val root = column(id, modify(TabClass.tabs, modifiers)) {
        row(modify(TabClass.header)) {
            scope.tabs.forEachIndexed { index, tab ->
                val button = p {
                    applyModifiers(TabClass.button)
                    attributes["data-tab"] = index.toString()
                    +tab.label
                }
                fun createContent(event: Event) {
                    val element = tabPanelElements.getOrNull(index) ?: return
                    console.log("creating content: $index")
                    val render = createRender(element, tab.content)
                    render.context.emitOnLoad()
                    tabPanelElements[index] = null
                }
                button.addEventListener("select-tab", ::createContent)
            }
        }
        box(modify(TabClass.viewport)) {
            scope.tabs.forEach { tab ->
                val element = box(modify(TabClass.panel))
                tabPanelElements.add(element)
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