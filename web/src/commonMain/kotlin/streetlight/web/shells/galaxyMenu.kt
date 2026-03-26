package streetlight.web.shells

import kabinet.console.globalConsole
import koala.css.BlurBackdrop
import koala.css.BorderRadius1
import koala.css.BorderRadius4
import koala.css.GlowShadow
import koala.css.Magic
import koala.css.PositionAnchor
import koala.css.PrimaryCardBg
import koala.css.SlideUp
import koala.css.SolidBg
import koala.css.WrapFlex
import koala.css.modify
import koala.css.setAnchorName
import koala.html.Id
import koala.html.TagAttribute
import koala.html.btn
import koala.html.button
import koala.html.card
import koala.html.popover
import koala.html.popoverContainer
import koala.html.row
import koala.html.setAttribute
import kotlinx.html.DIV
import streetlight.model.data.Galaxy
import streetlight.web.HomeRoute

fun DIV.galaxyMenu(
    galaxies: List<Galaxy>,
    currentGalaxy: Galaxy?,
) {
    val menuId = Id("menu")
    val myAnchor = PositionAnchor("my-anchor")

    popover(menuId, myAnchor, modify(Magic, SlideUp)) {
        card(modify(BlurBackdrop, BorderRadius4, PrimaryCardBg)) {
            row(modify(WrapFlex)) {
                if (currentGalaxy != null) {
                    btn("Home", HomeRoute)
                }
                galaxies.forEach {
                    if (it.name == currentGalaxy?.name) return@forEach
                    buttonOf(it)
                }
            }
        }
    }
    button("galaxies") {
        setAnchorName(myAnchor)
        setAttribute(TagAttribute.popoverTarget, menuId)
    }
}