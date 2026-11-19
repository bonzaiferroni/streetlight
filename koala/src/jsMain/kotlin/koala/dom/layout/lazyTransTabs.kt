package koala.dom.layout

import kampfire.model.MutableTap
import koala.dom.View
import koala.dom.ViewScope
import koala.dom.component.box
import koala.dom.component.column
import koala.dom.launchEffect
import koala.dom.mountChildView
import koala.interop.initTransTabs
import koala.interop.readParamIndex
import koala.interop.readStorageIndex
import koala.layout.TabsStyle
import koala.layout.TransTabsStyle
import koala.layout.configureTransTabsContainer
import koala.layout.configureTransTabsHeader
import koala.modifier.*
import koala.page.Id
import kotlinx.coroutines.isActive
import web.html.HTMLDivElement
import web.html.HTMLElement

/**
 * [transTabs] whose panels are built the first time each is shown. With [isCached] `false`, a panel's view is disposed
 * as it leaves and built again when it returns.
 *
 * The selected tab follows [indexState] when given. A container with [storageKey] restores its selected tab from
 * `localStorage`, and one with [paramKey] from the url.
 */
fun ViewScope.lazyTransTabs(
    storageKey: String? = null,
    mod: Modifier? = null,
    viewportMod: Modifier? = null,
    indexState: MutableTap<Int>? = null,
    paramKey: String? = null,
    id: Id? = null,
    isCached: Boolean = true,
    content: LazyTabScope.() -> Unit,
): HTMLDivElement {
    val scope = LazyTabScope()
    scope.content()
    val tabCount = scope.tabs.size
    val initialIndex = (indexState?.now
        ?: paramKey?.let { readParamIndex(it) }
        ?: storageKey?.let { readStorageIndex(it) }
        ?: 0).coerceIn(0, tabCount - 1)
    val panels = arrayOfNulls<HTMLElement>(tabCount)
    val views = arrayOfNulls<View>(tabCount)
    var shownIndex = initialIndex

    fun render(index: Int) {
        if (!isCached && index != shownIndex) {
            views[shownIndex]?.dispose()
            views[shownIndex] = null
        }
        shownIndex = index
        if (views[index] != null) return
        views[index] = mountChildView("panel", panels[index]!!, scope.tabs[index].content)
    }

    val container = column {
        configureTransTabsContainer(storageKey, mod, initialIndex, paramKey, id)
        configureTransTabsHeader(scope.tabs, initialIndex)

        box(modify(viewportMod, TransTabsStyle.Viewport)) {
            scope.tabs.forEachIndexed { index, _ ->
                panels[index] = column(TransTabsStyle.Panel) {
                    if (index == initialIndex) addModifiers(TabsStyle.IsActive)
                }
            }
        }
    }

    render(initialIndex)

    indexState?.let {
        container.observeAttribute(TabsStyle.Index) {
            val index = it ?: return@observeAttribute
            if (indexState.now != index) indexState.set(index)
        }

        launchEffect("lazyTransTabs") {
            indexState.flow.collect { index ->
                container.setAttribute(TabsStyle.Index.to(index))
            }
        }
    }

    // a show queued while this view lives may run once it is gone, so it builds nothing then
    val tabsScope = contentScope
    initTransTabs(container) { index -> if (tabsScope.isActive) render(index) }

    return container
}
