package koala.dom

import koala.css.Css
import koala.css.CssClass
import koala.css.ModifierSet
import koala.css.modify
import koala.html.Id
import koala.html.TabClass
import koala.html.TabScope
import koala.html.tabsContent
import kotlinx.html.id
import org.w3c.dom.HTMLDivElement

fun DOMContext.tabs(
    id: Id,
    modifiers: ModifierSet? = null,
    content: TabScope.() -> Unit,
): HTMLDivElement {
    val scope = TabScope()
    scope.content()
    val root = column(id, modify(TabClass.tabs, modifiers)) {
        tabsContent(scope)
    }

    initTabs(root)

    return root
}