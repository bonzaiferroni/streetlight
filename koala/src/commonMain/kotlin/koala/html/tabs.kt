package koala.html

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import koala.css.*
import kotlinx.html.p

fun FlowContent.tabs(
    id: Id? = null,
    mod: ModifierSet? = null,
    content: TabScope.() -> Unit,
) {
    val scope = TabScope()
    scope.content()
    column(modify(TabStyle.Container, mod)) {
        id?.let {
            setId(it)
        }
        row(modify(TabStyle.header)) {
            scope.tabs.forEachIndexed { index, tab ->
                p {
                    addModifiers(TabStyle.Button)
                    attributes["data-tab"] = index.toString()
                    if (tab.isDefault) {
                        attributes["is-default"] = ""
                    }
                    tab.colorScheme?.let {
                        setStyle(Property.ColorScheme.to(it))
                    }
                    +tab.label
                }
            }
        }
        box(modify(TabStyle.Viewport)) {
            scope.tabs.forEach { tab ->
                val content = tab.content
                box(modify(TabStyle.Panel)) {
                    content()
                }
            }
        }
    }
}

object TabStyle {
    val Container = Class("tabs")
    val Button = Class("tabs-button")
    val header = Class("tabs-header")
    val Viewport = Class("tabs-viewport")
    val Panel = Class("tabs-panel")

    val tabIndex = intAttributeOf("tab") // td: didn't work, figure out why
    val IsDefault = booleanAttributeOf("is-default")

    val IndexDelta = Property<Int>("index-delta")
}

fun TabScope.tab(
    label: String,
    isDefault: Boolean = false,
    content: DIV.() -> Unit
) {
    add(label, isDefault, content)
}

class TabScope {
    private val _tabs: MutableList<Tab> = mutableListOf()
    val tabs: List<Tab> = _tabs

    val initialIndex get() = tabs.indexOfFirst { it.isDefault }.takeIf { it >= 0 } ?: 0

    fun add(label: String, isDefault: Boolean, content: DIV.() -> Unit) {
        _tabs.add(Tab(
            label = label,
            isDefault = isDefault,
            content = content
        ))
    }
}

data class Tab(
    val label: String,
    val id: Id = Id(label),
    val isDefault: Boolean = false,
    val colorScheme: String? = null,
    val content: DIV.() -> Unit
)