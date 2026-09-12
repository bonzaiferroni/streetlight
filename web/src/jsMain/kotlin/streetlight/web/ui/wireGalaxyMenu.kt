package streetlight.web.ui

import kampfire.model.thumb
import koala.css.AlignItemsCenter
import koala.css.Aspect1
import koala.css.BorderRadius50P
import koala.css.Height5
import koala.css.HoverBg
import koala.css.MaxHeight64
import koala.css.MinWidth8
import koala.css.Padding1
import koala.css.Width5
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.append
import koala.dom.box
import koala.dom.column
import koala.dom.grid
import koala.dom.onClick
import koala.dom.row
import koala.dom.textBlock
import koala.html.image
import kotlinx.css.GridTemplateColumns
import streetlight.model.data.Galaxy
import streetlight.web.layouts.route
import streetlight.web.model.Fleet
import streetlight.web.shells.GalaxyMenu
import kotlin.random.Random

fun ViewScope.wireGalaxyMenu() {
    val fleet = app.get<Fleet<Galaxy>>()
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