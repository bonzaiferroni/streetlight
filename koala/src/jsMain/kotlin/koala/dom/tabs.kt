package koala.dom

import koala.css.Css
import koala.css.CssClass
import koala.html.Id
import koala.html.TabScope
import koala.html.tabsContent
import kotlinx.html.id
import org.w3c.dom.HTMLDivElement

fun DOMContext.tabs(
    id: Id,
    vararg modifiers: CssClass,
    content: TabScope.() -> Unit,
): HTMLDivElement {
    val scope = TabScope()
    scope.content()
    val root = column(Css("tabs"), *modifiers) {
        this.id = id.value
        tabsContent(scope)
    }

    initTabs(root)

    return root
}