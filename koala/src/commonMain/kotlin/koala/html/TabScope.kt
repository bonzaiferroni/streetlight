package koala.html

import kotlinx.html.DIV

/** Collects the tabs of a [tabs] block. */
class TabScope {
    val tabs: List<Tab> field = mutableListOf()

    /** Adds a tab labeled [label], holding what [content] builds. */
    fun tab(label: String, content: DIV.() -> Unit) {
        tabs.add(Tab(
            label = label,
            content = content
        ))
    }
}

/** A tab: its label, its optional color scheme, and the content of its panel. */
data class Tab(
    override val label: String,
    val id: Id = Id(label),
    override val colorScheme: String? = null,
    val content: DIV.() -> Unit
): TabHeader

/** The part of a tab its button shows. */
interface TabHeader {
    val label: String
    val colorScheme: String?
}