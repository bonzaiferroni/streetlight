package koala.html

import kotlinx.html.DIV

class TabScope {
    val tabs: List<Tab> field = mutableListOf()

    fun tab(label: String, content: DIV.() -> Unit) {
        tabs.add(Tab(
            label = label,
            content = content
        ))
    }
}

data class Tab(
    override val label: String,
    val id: Id = Id(label),
    override val colorScheme: String? = null,
    val content: DIV.() -> Unit
): TabHeader

interface TabHeader {
    val label: String
    val colorScheme: String?
}