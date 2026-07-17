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
    mod: ModifierSet? = null,
    viewportMod: ModifierSet? = null,
    onChangeTab: ((Int) -> Unit)? = null,
    tabFlow: Flow<Int>? = null,
    defaultTab: Int? = null,
    content: TabScope<T>.() -> Unit,
): HTMLDivElement {
    val scope = TabScope(content = content)
    var viewport: HTMLElement? = null

    val root = column(id, modify(TabClass.tabs, mod)) {
        tabsHeader(scope)
        viewport = tabsViewport(viewportMod)
    }

    scope.build(viewport!!)

    var currentTab = defaultTab ?: scope.tabs.firstOrNull()?.label

    defaultTab?.let {
        root.setAttribute(Attribute.TabIndex.to(it))
    }

    onChangeTab?.let {
        root.observeAttribute(Attribute.TabIndex) {
            val name = it ?: return@observeAttribute
            if (name == currentTab) return@observeAttribute
            currentTab = name
            onChangeTab(name)
        }
    }

    if (this is ViewScope) {
        tabFlow?.let { flow ->
            parentScope.launch {
                flow.collect { name ->
                    if (name == currentTab) return@collect
                    currentTab = name
                    root.setAttribute(Attribute.TabIndex.to(name))
                }
            }
        }
    } else {
        if (tabFlow != null) error("tabFlow requires AppScope receiver")
    }

    initTabs(root, viewport)

    return root
}

fun <T : TagScope> T.tabsHeader(tabScope: TabScope<T>) = div(modify(TabClass.header)) {
    tabScope.tabs.forEachIndexed { index, tab ->
        val button = p {
            addModifiers(TabClass.button)
            attributes["data-tab"] = index.toString()
            tab.colorScheme?.let {
                setStyle(Property.ColorScheme.to(it))
            }
            +tab.label
        }
        button.addEventListener("select-tab", {
            tabScope.renderTab(this@tabsHeader, index)
        })
    }
}

fun TagScope.tabsViewport(
    mod: ModifierSet? = null,
) = box(modify(TabClass.viewport, mod))