package koala.layout

import kotlinx.html.DIV
import koala.SvgPack
import koala.component.button

/** Collects the tabs of a [tabs] block. */
class TabScope {
    val tabs: List<Tab> field = mutableListOf()

    /** Adds a tab labeled [label] after an optional [icon], holding what [content] builds. */
    fun tab(label: String, icon: SvgPack? = null, content: DIV.() -> Unit) {
        tabs.add(Tab(
            label = label,
            icon = icon,
            content = content
        ))
    }
}

/** A tab: its label, its optional icon and color scheme, and the content of its panel. */
data class Tab(
    override val label: String,
    override val icon: SvgPack? = null,
    override val colorScheme: String? = null,
    val content: DIV.() -> Unit
): TabHeader

/** The part of a tab its button shows. */
interface TabHeader {
    val label: String
    val icon: SvgPack?
    val colorScheme: String?
}