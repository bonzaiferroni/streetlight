package koala.dom

import koala.css.*
import koala.html.Attribute
import koala.html.Id
import koala.html.TabStyle
import kampfire.model.MutableTap
import kotlinx.html.js.p
import web.html.HTMLDivElement
import web.html.HTMLElement

fun ViewScope.tabs(
    id: Id? = null,
    mod: ModifierSet? = null,
    viewportMod: ModifierSet? = null,
    indexState: MutableTap<Int>? = null,
    defaultTab: Int? = null,
    content: DomTabScope.() -> Unit,
): HTMLDivElement {
    val tabScope = DomTabScope(content = content)
    var viewport: HTMLElement? = null

    val root = column(id, modify(TabStyle.Container, mod)) {
        tabsHeader(tabScope)
        viewport = tabsViewport(viewportMod)
    }

    tabScope.build(viewport!!)

    val initialTab = indexState?.now ?: defaultTab
    initialTab?.let {
        root.setAttribute(Attribute.TabIndex.to(it))
    }

    indexState?.let { field ->
        var currentTab = field.now

        fun display(value: Int) {
            currentTab = value
            root.setAttribute(Attribute.TabIndex.to(value))
        }

        root.observeAttribute(Attribute.TabIndex) {
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

    initTabs(root, viewport)

    return root
}

fun ViewScope.tabsHeader(tabScope: DomTabScope) = div(modify(TabStyle.header)) {
    tabScope.tabs.forEachIndexed { index, tab ->
        val button = p {
            addModifiers(TabStyle.Button)
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

fun AppendScope.tabsViewport(
    mod: ModifierSet? = null,
) = box(modify(TabStyle.Viewport, mod))