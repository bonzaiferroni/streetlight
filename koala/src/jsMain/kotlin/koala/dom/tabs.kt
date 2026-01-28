package koala.dom

import koala.css.Css
import koala.css.CssClass
import koala.html.TabScope
import koala.html.tabsContent
import org.w3c.dom.HTMLDivElement

fun DOMContext.tabs(
    vararg modifiers: CssClass,
    content: TabScope.() -> Unit,
): HTMLDivElement {
    val scope = TabScope()
    scope.content()
    val root = column(Css("tabs"), *modifiers) {
        tabsContent(scope)
    }

    initTabs(root)

    return root
}