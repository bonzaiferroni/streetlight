package koala.html

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import koala.css.*
import kotlinx.html.p

fun FlowContent.tabs(
    id: Id,
    modifiers: ModifierSet? = null,
    content: TabScope.() -> Unit,
) {
    val scope = TabScope()
    scope.content()
    column(id, modify(TabClass.tabs, modifiers)) {
        row(modify(TabClass.header)) {
            scope.tabs.forEachIndexed { index, tab ->
                p {
                    addModifiers(TabClass.button)
                    attributes["data-tab"] = index.toString()
                    if (tab.isDefault) {
                        attributes["is-default"] = ""
                    }
                    +tab.label
                }
            }
        }
        box(modify(TabClass.viewport)) {
            scope.tabs.forEach { tab ->
                val content = tab.content
                box(modify(TabClass.panel)) {
                    content()
                }
            }
        }
    }
}

object TabClass {
    val tabs = Class("tabs")
    val button = Class("tabs-button")
    val header = Class("tabs-header")
    val viewport = Class("tabs-viewport")
    val panel = Class("tabs-panel")
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
    val content: DIV.() -> Unit
)