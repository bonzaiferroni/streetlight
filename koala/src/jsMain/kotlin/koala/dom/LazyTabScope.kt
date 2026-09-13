package koala.dom

import koala.html.Id
import koala.html.TabHeader

class LazyTabScope() {
    val tabs: List<LazyTab> field = mutableListOf()

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

data class LazyTab(
    override val label: String,
    val id: Id = Id(label),
    override val colorScheme: String?,
    val content: ViewScope.() -> Unit
): TabHeader