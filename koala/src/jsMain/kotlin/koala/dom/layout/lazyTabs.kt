package koala.dom.layout

import kampfire.model.MutableTap
import koala.modifier.*
import koala.page.Id
import koala.interop.initTabs
import web.html.HTMLDivElement
import web.html.HTMLElement
import koala.dom.ViewScope
import koala.dom.launchEffect
import koala.dom.mountChildView
import koala.layout.TabsStyle
import koala.layout.configureTabsContainer
import koala.layout.configureTabsHeader
import koala.dom.component.box
import koala.dom.component.column
import koala.interop.readParamIndex
import koala.interop.readStorageIndex

/**
 * Tabs whose panels are built the first time each is shown.
 *
 * The selected tab follows [indexState] when given, and otherwise starts at [defaultTab]. A container with
 * [storageKey] restores its selected tab from `localStorage`.
 */
fun ViewScope.lazyTabs(
    storageKey: String? = null,
    mod: Modifier? = null,
    viewportMod: Modifier? = null,
    indexState: MutableTap<Int>? = null,
    defaultTab: Int? = null,
    paramKey: String? = null,
    id: Id? = null,
    content: LazyTabScope.() -> Unit,
): HTMLDivElement {
    val scope = LazyTabScope()
    scope.content()
    val tabCount = scope.tabs.size
    val initialIndex = indexState?.now
        ?: paramKey?.let { readParamIndex(it) }
        ?: storageKey?.let { readStorageIndex(it) }
        ?: defaultTab ?: 0
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
        configureTabsContainer(storageKey, mod, initialIndex, paramKey, id)
        configureTabsHeader(scope.tabs, initialIndex)

        box(modify(viewportMod, TabsStyle.Viewport)) {
            scope.tabs.forEachIndexed { index, _ ->
                panelCache[index] = column(TabsStyle.Panel) {
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