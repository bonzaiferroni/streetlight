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

fun <T : TagScope> T.tabs(
    id: Id? = null,
    modifiers: ModifierSet? = null,
    onChangeTab: ((String) -> Unit)? = null,
    tabFlow: Flow<String>? = null,
    defaultTab: String? = null,
    content: TabScope<T>.() -> Unit,
): HTMLDivElement {
    val scope = TabScope(content = content)
    var viewport: HTMLElement? = null

    val root = column(id, modify(TabClass.tabs, modifiers)) {
        tabsHeader(scope)
        viewport = tabsViewport()
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

    if (this is AppScope) {
        tabFlow?.let { flow ->
            parentScope.launch {
                flow.collect { name ->
                    if (name == currentTab) return@collect
                    currentTab = name
                    root.setAttribute(Attribute.TabName.to(name))
                }
            }
        }
    } else {
        if (tabFlow != null) error("tabflow requires ScopedDOM receiver")
    }

    scope.build(viewport!!)

    initTabs(root, viewport)

    return root
}

fun <T : TagScope> T.tabsHeader(tabScope: TabScope<T>) = row(modify(TabClass.header)) {
    tabScope.tabs.forEachIndexed { index, tab ->
        val button = p {
            addModifiers(TabClass.button)
            attributes["data-tab"] = index.toString()
            +tab.label
        }
        button.addEventListener("select-tab", {
            tabScope.selectTab(this@tabsHeader, index)
        })
    }
}

fun TagScope.tabsViewport() = box(modify(TabClass.viewport))