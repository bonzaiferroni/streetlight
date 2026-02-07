package koala.html

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import koala.css.*
import kotlinx.html.TagConsumer
import kotlinx.html.p

fun FlowContent.tabs(
    modifiers: ModifierSet? = null,
    content: TabScope.() -> Unit,
) {
    val scope = TabScope()
    scope.content()
    column(modify(TabClass.tabs, modifiers)) {
        tabsContent(scope)
    }
}

fun FlowContent.tabsContent(scope: TabScope) {
    row(modify(TabClass.header)) {
        scope.tabs.forEachIndexed { index, tab ->
            p {
                applyModifiers(TabClass.button)
                attributes["data-tab"] = index.toString()
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

object TabClass {
    val tabs = Css("tabs")
    val button = Css("tabs-button")
    val header = Css("tabs-header")
    val viewport = Css("tabs-viewport")
    val panel = Css("tabs-panel")
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