package koala.html

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import koala.css.*

fun FlowContent.tabs(
    vararg modifiers: CssClass,
    content: TabScope.() -> Unit,
) {
    val scope = TabScope()
    scope.content()
    column(Css("tabs"), *modifiers) {
        row(Css("tabs-header")) {
            scope.tabs.forEachIndexed { index, tab ->
                label(tab.label, Css("tabs-button")) {
                    attributes["data-tab"] = index.toString()
                }
            }
        }
        box(Css("tabs-viewport")) {
            scope.tabs.forEach { tab ->
                val content = tab.content
                box(Css("tabs-panel")) {
                    content()
                }
            }
        }
    }
}

fun TabScope.tab(
    label: String,
    content: DIV.() -> Unit
) {
    add(label, content)
}

class TabScope {
    private val _tabs: MutableList<Tab> = mutableListOf()
    val tabs: List<Tab> = _tabs

    fun add(label: String, content: DIV.() -> Unit) {
        _tabs.add(Tab(
            label = label,
            content = content
        ))
    }
}

data class Tab(
    val label: String,
    val id: Id = Id(label),
    val content: DIV.() -> Unit
)