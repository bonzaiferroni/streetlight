package streetlight.web.interop

import koala.dom.ViewScope
import koala.dom.layout.column
import koala.modifier.getAttribute
import koala.dom.layout.popoverOption
import koala.model.LabeledRoute
import koala.modifier.Attribute
import streetlight.model.ui.StarRoute
import streetlight.web.page.PopoverId
import streetlight.web.dom.component.PopoverMenuMod
import streetlight.web.dom.component.popoverMenu

/** The menu of a star named on the page, to visit or message them. */
fun ViewScope.wireStarMenu() {
    popoverMenu(
        popoverId = PopoverId.StarMenu,
        transform = { it.getAttribute(Attribute.Username) }
    ) { username ->
        column(PopoverMenuMod.Column) {
            popoverOption(LabeledRoute(StarRoute(username)))
            popoverOption("Message") { startMessage(username) }
        }
    }
}

