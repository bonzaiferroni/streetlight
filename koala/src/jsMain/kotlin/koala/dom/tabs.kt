package koala.dom

import koala.modifier.*
import koala.html.Id
import kampfire.model.MutableTap
import koala.html.TabScope
import koala.html.TabsStyle
import koala.html.configureTabs
import koala.interop.initTabs
import web.html.HTMLDivElement

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