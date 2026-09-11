package streetlight.web.shells

import koala.css.modify
import koala.html.Id
import koala.html.ButtonPopover
import koala.html.btn
import koala.html.button
import koala.html.buttonPopover
import koala.html.card
import koala.html.row
import koala.html.setJsonData
import koala.html.setPopoverTarget
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.ui.HomeRoute
import streetlight.web.layouts.GalaxyKey
import streetlight.web.ui.PostMenu

fun FlowContent.galaxyMenu() {
    button("galaxies") {
        setPopoverTarget(GalaxyMenu.PopoverId)
    }
}

object GalaxyMenu {
    val PopoverId = Id("galaxy-menu-popover")
}