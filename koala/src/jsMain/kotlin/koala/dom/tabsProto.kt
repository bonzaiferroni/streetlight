package koala.dom

import kampfire.model.MutableTap
import koala.css.ModifierSet
import koala.html.Id
import koala.html.TabScope
import koala.html.TabsProtoStyle
import koala.html.configureTabsProto
import koala.interop.initTabsProto
import web.html.HTMLDivElement

fun ViewScope.tabsProto(
    id: Id? = null,
    mod: ModifierSet? = null,
    viewportMod: ModifierSet? = null,
    indexState: MutableTap<Int>? = null,
    content: TabScope.() -> Unit,
): HTMLDivElement {
    val container = column {
        configureTabsProto(id, mod, viewportMod, content)
    }

    indexState?.let { field ->
        var currentTab = field.now

        fun display(value: Int) {
            currentTab = value
            container.setAttribute(TabsProtoStyle.Index.to(value))
        }

        container.observeAttribute(TabsProtoStyle.Index) {
            val name = it ?: return@observeAttribute
            if (name == currentTab) return@observeAttribute
            field.set(name)
            display(field.now)
        }

        launchEffect("tabs") {
            field.flow.collect {
                display(it)
            }
        }
    }

    initTabsProto(container)

    return container
}