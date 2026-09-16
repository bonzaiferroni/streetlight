package streetlight.web.shells

import koala.html.Id
import koala.html.button
import koala.modifier.setPopoverTarget
import kotlinx.html.FlowContent

fun FlowContent.galaxyMenu() {
    button("galaxies") {
        setPopoverTarget(GalaxyMenu.PopoverId)
    }
}

object GalaxyMenu {
    val PopoverId = Id("galaxy-menu-popover")
}