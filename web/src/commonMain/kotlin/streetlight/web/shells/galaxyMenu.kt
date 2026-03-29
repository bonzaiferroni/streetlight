package streetlight.web.shells

import koala.css.BlurBackdrop
import koala.css.BorderRadius4
import koala.css.Magic
import koala.css.PositionAnchor
import koala.css.PrimaryCardBg
import koala.css.SlideUp
import koala.css.WrapFlex
import koala.css.modify
import koala.css.setAnchorName
import koala.html.Id
import koala.html.Attribute
import koala.html.btn
import koala.html.button
import koala.html.card
import koala.html.popover
import koala.html.row
import koala.html.setAttribute
import koala.html.setJsonData
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer
import streetlight.model.data.Galaxy
import streetlight.web.HomeRoute
import streetlight.web.layouts.GalaxyKey

fun DIV.galaxyMenu(
    galaxies: List<Galaxy>,
    currentGalaxy: Galaxy?,
) {
    popover(GalaxyMenuKey.Id, GalaxyMenuKey.Anchor, modify(Magic, SlideUp)) {
        card(modify(BlurBackdrop, BorderRadius4, PrimaryCardBg)) {
            setJsonData(GalaxyKey.TopGalaxies, galaxies)

            row(GalaxyMenuKey.RowMods) {
                galaxyMenuItems(galaxies, currentGalaxy)
            }
        }
    }
    button("☰ galaxies") {
        setAnchorName(GalaxyMenuKey.Anchor)
        setAttribute(Attribute.PopoverTarget, GalaxyMenuKey.Id.value)
    }
}

fun DIV.galaxyMenuItems(
    galaxies: List<Galaxy>,
    currentGalaxy: Galaxy?,
) {
    if (currentGalaxy != null) {
        btn("Home", HomeRoute)
    }
    galaxies.forEach {
        if (it.name == currentGalaxy?.name) return@forEach
        buttonOf(it)
    }
}

object GalaxyMenuKey {
    val Id = Id("galaxy-menu")
    val Anchor = PositionAnchor("galaxy-menu-anchor")
    val RowMods = modify(WrapFlex)
}