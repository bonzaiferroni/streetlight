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

/**
 * Tabs over panels, with the selected tab kept in step with [indexState] when given.
 *
 * A container with [id] restores its selected tab from `localStorage`.
 */
fun ViewScope.tabs(
    id: Id? = null,
    mod: Modifier? = null,
    viewportMod: Modifier? = null,
    indexState: MutableTap<Int>? = null,
    content: TabScope.() -> Unit,
): HTMLDivElement {
    val container = column {
        configureTabs(id, mod, viewportMod, indexState?.now, content)
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