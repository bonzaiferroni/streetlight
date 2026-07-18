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

fun ViewScope.tabs(
    id: Id? = null,
    mod: ModifierSet? = null,
    viewportMod: ModifierSet? = null,
    onChangeTab: ((Int) -> Unit)? = null,
    indexFlow: Flow<Int>? = null,
    defaultTab: Int? = null,
    content: TabScope.() -> Unit,
): HTMLDivElement {
    val tabScope = TabScope(content = content)
    var viewport: HTMLElement? = null

    val root = column(id, modify(TabClass.tabs, mod)) {
        tabsHeader(tabScope)
        viewport = tabsViewport(viewportMod)
    }

    tabScope.build(viewport!!)

    var currentTab = defaultTab ?: tabScope.tabs.firstOrNull()?.label

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

    indexFlow?.let { flow ->
        scope.launch {
            flow.collect { name ->
                if (name == currentTab) return@collect
                currentTab = name
                root.setAttribute(Attribute.TabIndex.to(name))
            }
        }
    }

    initTabs(root, viewport)

    return root
}

fun ViewScope.tabsHeader(tabScope: TabScope) = div(modify(TabClass.header)) {
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
            tabScope.createTab(this@tabsHeader, index)
        })
    }
}

fun TagScope.tabsViewport(
    mod: ModifierSet? = null,
) = box(modify(TabClass.viewport, mod))