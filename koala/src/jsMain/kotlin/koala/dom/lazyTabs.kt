package koala.dom

import kampfire.model.MutableTap
import koala.modifier.ModifierSet
import koala.modifier.addModifiers
import koala.modifier.modify
import koala.html.Id
import koala.html.TabsStyle
import koala.html.box
import koala.html.configureTabsContainer
import koala.html.configureTabsHeader
import koala.interop.initTabs
import web.html.HTMLDivElement
import web.html.HTMLElement

fun ViewScope.lazyTabs(
    id: Id? = null,
    mod: ModifierSet? = null,
    viewportMod: ModifierSet? = null,
    indexState: MutableTap<Int>? = null,
    defaultTab: Int? = null,
    content: LazyTabScope.() -> Unit,
): HTMLDivElement {
    val scope = LazyTabScope()
    scope.content()
    val tabCount = scope.tabs.size
    val initialIndex = indexState?.now ?: defaultTab ?: 0
    val panelCache = Array<HTMLElement?>(tabCount) { null }
    val isRendered = Array(tabCount) { false }

    fun render(index: Int) {
        if (isRendered[index]) return
        isRendered[index] = true
        val panel = panelCache[index]!!
        val tab = scope.tabs[index]
        mountChildView("panel", panel, tab.content)
    }

    val container = column {
        configureTabsContainer(id, mod, initialIndex)
        configureTabsHeader(scope.tabs, initialIndex)

        box(modify(viewportMod, TabsStyle.Viewport)) {
            scope.tabs.forEachIndexed { index, _ ->
                panelCache[index] = column(modify(TabsStyle.Panel)) {
                    if (index == initialIndex) {
                        addModifiers(TabsStyle.IsActive)
                    }
                }
            }
        }
    }

    render(initialIndex)

    container.observeAttribute(TabsStyle.Index) {
        val index = it ?: return@observeAttribute
        render(index)
        if (indexState?.now != index) indexState?.set(index)
    }

    indexState?.let { field ->
        launchEffect("tabs") {
            field.flow.collect { index ->
                container.setAttribute(TabsStyle.Index.to(index))
            }
        }
    }

    initTabs(container)

    return container
}