package streetlight.web.shells

import koala.page.Id
import koala.component.button
import koala.modifier.Zen
import koala.modifier.setPopoverTarget
import kotlinx.html.FlowContent

fun FlowContent.galaxyMenu() {
    button("galaxies", Zen) {
        setPopoverTarget(GalaxyMenu.PopoverId)
    }
}

object GalaxyMenu {
    val PopoverId = Id("galaxy-menu-popover")
}