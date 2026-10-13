package streetlight.web.interop

import kampfire.model.thumb
import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.append
import koala.dom.component.box
import koala.dom.component.grid
import koala.dom.onClick
import koala.dom.component.row
import koala.dom.component.textBlock
import koala.component.image
import kotlinx.css.GridTemplateColumns
import streetlight.web.model.route
import streetlight.web.model.DataCache
import streetlight.web.component.GalaxyMenu
import streetlight.web.page.PopoverId
import streetlight.web.utility.element
import streetlight.web.dom.component.popoverMenu
import streetlight.web.model.portal
import streetlight.web.component.starToggle
import streetlight.web.model.toaster

/** The menu of the star's galaxies. */
fun ViewScope.wireGalaxyMenu() {
    val fleet = app.get<DataCache>().galaxyFleet
    popoverMenu(
        popoverId = GalaxyMenu.PopoverId,
        transform = { it },
    ) { _ ->
        val element = grid(GridTemplateColumns("auto auto"), modify(MaxHeight(64), AlignItemsCenter)) { }
        launchEffect {
            val galaxies = fleet.provisionFleet(toaster) ?: return@launchEffect
            element.append {
                galaxies.forEach { galaxy ->
                    row(modify(AlignItemsCenter, Padding(1), HoverBg)) {
                        image(galaxy.image?.variants.thumb, modify(Height(5), Aspect1, BorderRadius50P))
                        textBlock(galaxy.name)
                    }.onClick { portal.go(galaxy.route) }

                    box(modify(Padding(1), MinWidth(8))) {
                        starToggle(galaxy)
                    }
                }
            }
        }
    }
}