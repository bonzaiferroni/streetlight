package koala.dom

import koala.html.Id
import koala.html.TabHeader

/** Collects the tabs of a [lazyTabs] block. */
class LazyTabScope() {
    val tabs: List<LazyTab> field = mutableListOf()

    /** Adds a tab labeled [label], whose panel is built by [content] the first time it is shown. */
    fun tab(
        label: String,
        colorScheme: String? = null,
        content: ViewScope.() -> Unit
    ) {
        tabs.add(LazyTab(
            label = label,
            colorScheme = colorScheme,
            content = content
        ))
    }
}

/** A tab whose panel is built the first time it is shown. */
data class LazyTab(
    override val label: String,
    val id: Id = Id(label),
    override val colorScheme: String?,
    val content: ViewScope.() -> Unit
): TabHeader