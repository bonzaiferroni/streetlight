package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.column
import koala.modifier.getAttribute
import koala.dom.popoverOption
import koala.modifier.Attribute
import streetlight.model.ui.StarRoute

fun ViewScope.wireStarMenu() {
    popoverMenu(
        popoverId = PopoverId.StarMenu,
        transform = { it.getAttribute(Attribute.Username) }
    ) { username ->
        column(PopoverMenuMod.Column) {
            popoverOption(StarRoute(username))
            popoverOption("Message") { startMessage(username) }
        }
    }
}

