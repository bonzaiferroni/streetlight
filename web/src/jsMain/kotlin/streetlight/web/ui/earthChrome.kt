package streetlight.web.ui

import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.Aspect1
import koala.css.BlurBackdrop
import koala.css.Padding1
import koala.css.PaperGradientBg
import koala.css.PointerEventsAuto
import koala.css.Width5
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.icon
import koala.dom.onClick
import koala.dom.row
import streetlight.model.ui.HomeRoute
import streetlight.web.model.Earth

private fun ViewScope.boundsHud(model: Earth) {
    // flowBlock(model.summaryFlow) { summary ->
    //     if (summary.isNullOrEmpty()) return@flowBlock
    //     column(modify(WidthFitContent, Gap0)) {
    //         filigree {
    //             textBlock("In View", modify(OpacityHigh))
    //         }
    //         row(modify(Gap2)) {
    //             summary.forEach { (markerType, count) ->
    //                 textBlock {
    //                     span(markerType ?: "unknown")
    //                     span(" | ", modify(OpacityLow))
    //                     span(count.toString())
    //                 }
    //             }
    //         }
    //     }
    // }
}

fun ViewScope.earthHeaderLegacy(model: Earth) {
    val iconMod = modify(Width5, Aspect1)
    row(modify(EarthStyle.Header, AlignItemsCenter, PaperGradientBg, Padding1, PointerEventsAuto, BlurBackdrop)) {
        // flowBlock(model.mapFlow, modify(Flex1)) { map ->
        //     when (map) {
        //         null -> row {
        //             icon(SvgFile.Helm, iconMod)
        //             logo()
        //         }
        //         else -> row(modify(AlignItemsCenter)) {
        //             icon(SvgFile.ArrowLeft, iconMod).onClick {
        //                 portal.go(GalaxyMapRoute(null))
        //             }
        //             heading3(map.title, modify(LineHeight115, SingleLine, Bold))
        //         }
        //     }
        // }
        icon(SvgFile.GearLarge, iconMod).onClick { portal.go(HomeRoute) }
    }
}