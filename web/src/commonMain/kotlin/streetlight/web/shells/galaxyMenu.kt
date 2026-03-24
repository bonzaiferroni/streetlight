package streetlight.web.shells

import koala.css.Magic
import koala.css.PositionAnchor
import koala.css.SlideUp
import koala.css.SolidBg
import koala.css.WrapFlex
import koala.css.modify
import koala.css.setAnchorName
import koala.html.Id
import koala.html.TagAttribute
import koala.html.button
import koala.html.card
import koala.html.popover
import koala.html.popoverContainer
import koala.html.row
import koala.html.setAttribute
import kotlinx.html.DIV
import streetlight.model.data.Galaxy

fun DIV.galaxyMenu(
    galaxies: List<Galaxy>
) {
    val menuId = Id("menu")
    val myAnchor = PositionAnchor("my-anchor")

    popoverContainer(myAnchor)
    popover(menuId, myAnchor, modify(Magic, SlideUp)) {
        card(modify(SolidBg)) {
            row(modify(WrapFlex)) {
                galaxies.forEach {
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