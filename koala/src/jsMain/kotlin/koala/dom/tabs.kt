package koala.dom

import koala.css.*
import koala.html.Attribute
import koala.html.Id
import koala.html.TabClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.js.p
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

fun DOMRender.tabs(
    id: Id? = null,
    modifiers: ModifierSet? = null,
    onChangeTab: ((String) -> Unit)? = null,
    tabFlow: Flow<String>? = null,
    defaultTab: String? = null,
    content: TabScope.() -> Unit,
): HTMLDivElement {
    val scope = TabScope()
    scope.content()
    val tabPanelElements = Array<HTMLElement?>(3) { null }
    val renders = Array<RenderJob?>(3) { null }
    val root = column(id, modify(TabClass.tabs, modifiers)) {
        row(modify(TabClass.header)) {
            scope.tabs.forEachIndexed { index, tab ->
                val button = p {
                    addModifiers(TabClass.button)
                    attributes["data-tab"] = index.toString()
                    +tab.label
                }
                fun createTab() {
                    val element = tabPanelElements.getOrNull(index) ?: error("tab not found: $index")
                    val context = createRenderJob(element, tab.content)
                    renders[index] = context
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

    var currentTab = defaultTab ?: scope.tabs.firstOrNull()?.label

    defaultTab?.let {
        root.setAttribute(Attribute.TabName.to(it))
    }

    onChangeTab?.let {
        root.observeAttribute(Attribute.TabName) {
            val name = it ?: return@observeAttribute
            if (name == currentTab) return@observeAttribute
            currentTab = name
            onChangeTab(name)
        }
    }

    tabFlow?.let { flow ->
        renderScope.launch {
            flow.collect { name ->
                if (name == currentTab) return@collect
                currentTab = name
                root.setAttribute(Attribute.TabName.to(name))
            }
        }
    }

    initTabs(root)

    return root
}

fun TabScope.tab(
    label: String,
    content: DOMRender.() -> Unit
) {
    add(label, content)
}

class TabScope {
    private val _tabs: MutableList<Tab> = mutableListOf()
    val tabs: List<Tab> = _tabs

    fun add(label: String, content: DOMRender.() -> Unit) {
        _tabs.add(Tab(
            label = label,
            content = content
        ))
    }
}

data class Tab(
    val label: String,
    val id: Id = Id(label),
    val content: DOMRender.() -> Unit
)