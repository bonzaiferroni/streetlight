package koala.dom.layout

import koala.modifier.*
import koala.page.Id
import kampfire.model.MutableTap
import koala.interop.initTabs
import web.html.HTMLDivElement
import koala.dom.ViewScope
import koala.dom.launchEffect
import koala.layout.TabScope
import koala.layout.TabsStyle
import koala.layout.configureTabs
import koala.dom.component.column
import koala.interop.readParamIndex
import koala.interop.readStorageIndex

/**
 * Tabs over panels, with the selected tab kept in step with [indexState] when given.
 *
 * A container with [storageKey] restores its selected tab from `localStorage`.
 */
fun ViewScope.tabs(
    storageKey: String? = null,
    mod: Modifier? = null,
    viewportMod: Modifier? = null,
    indexState: MutableTap<Int>? = null,
    paramKey: String? = null,
    id: Id? = null,
    content: TabScope.() -> Unit,
): HTMLDivElement {
    val container = column {
        val initialIndex = indexState?.now
            ?: paramKey?.let { readParamIndex(it) }
            ?: storageKey?.let { readStorageIndex(it) }
        configureTabs(storageKey, mod, viewportMod, initialIndex, paramKey, id, content)
    }

    indexState?.let {
        container.observeAttribute(TabsStyle.Index) {
            val index = it ?: return@observeAttribute
            if (indexState.now != index) indexState.set(index)
        }

        launchEffect("tabs") {
            indexState.flow.collect { index ->
                container.setAttribute(TabsStyle.Index.to(index))
            }
        }
    }

    initTabs(container)

    return container
}