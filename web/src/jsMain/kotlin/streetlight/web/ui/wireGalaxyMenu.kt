package streetlight.web.ui

import kampfire.model.thumb
import koala.modifier.AlignItemsCenter
import koala.modifier.Aspect1
import koala.modifier.BorderRadius50P
import koala.modifier.Height5
import koala.modifier.HoverBg
import koala.modifier.MaxHeight64
import koala.modifier.MinWidth8
import koala.modifier.Padding1
import koala.modifier.modify
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

fun ViewScope.wireGalaxyMenu() {
    val fleet = app.get<DataCache>().galaxyFleet
    popoverMenu(
        popoverId = GalaxyMenu.PopoverId,
        transform = { it },
    ) { _ ->
        val element = grid(GridTemplateColumns("auto auto"), modify(MaxHeight64, AlignItemsCenter)) { }
        launchEffect {
            val galaxies = fleet.provisionFleet(toaster) ?: return@launchEffect
            element.append {
                galaxies.forEach { galaxy ->
                    row(modify(AlignItemsCenter, Padding1, HoverBg)) {
                        image(galaxy.image?.variants.thumb, modify(Height5, Aspect1, BorderRadius50P))
                        textBlock(galaxy.name)
                    }.onClick { portal.go(galaxy.route) }

                    box(modify(Padding1, MinWidth8)) {
                        starToggle(galaxy)
                    }
                }
            }
        }
    }
}