package koala.dom.layout

import koala.SvgPack
import koala.dom.ViewScope
import koala.layout.TabHeader

/** Collects the tabs of a [lazyTabs] block. */
class LazyTabScope() {
    val tabs: List<LazyTab> field = mutableListOf()

    /** Adds a tab labeled [label] after an optional [icon], whose panel is built by [content] the first time it is shown. */
    fun tab(
        label: String,
        icon: SvgPack? = null,
        colorScheme: String? = null,
        content: ViewScope.() -> Unit
    ) {
        tabs.add(LazyTab(
            label = label,
            icon = icon,
            colorScheme = colorScheme,
            content = content
        ))
    }
}

/** A tab whose panel is built the first time it is shown. */
data class LazyTab(
    override val label: String,
    override val icon: SvgPack?,
    override val colorScheme: String?,
    val content: ViewScope.() -> Unit
): TabHeader