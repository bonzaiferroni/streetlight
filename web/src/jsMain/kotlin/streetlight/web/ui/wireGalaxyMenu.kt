package streetlight.web.ui

import kampfire.model.thumb
import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.append
import koala.dom.box
import koala.dom.grid
import koala.dom.onClick
import koala.dom.row
import koala.dom.textBlock
import koala.html.image
import kotlinx.css.GridTemplateColumns
import streetlight.web.layouts.route
import streetlight.web.model.DataCache
import streetlight.web.shells.GalaxyMenu

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