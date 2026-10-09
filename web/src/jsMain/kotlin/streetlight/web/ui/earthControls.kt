package streetlight.web.ui

import koala.SvgFile
import koala.dom.*
import koala.html.IconStyle
import koala.html.spacer
import koala.modifier.*
import streetlight.model.data.EventTag
import streetlight.web.model.Earth
import web.html.HTMLDivElement

fun ViewScope.earthControls(model: Earth) {
    row(modify(AlignItemsCenter, Padding(1))) {
        val buttonMod = modify(PointerEventsAuto, OpacityHigh, EarthStyle.MoveDimmer)
        val iconButtonMod = modify(buttonMod, IconStyle.DefaultMod)
        button(mod = buttonMod) {
            setPopoverTarget(EarthStyle.TagMenuId, true)
            icon(SvgFile.Label)
            flowBlock(model.tagState, modify(MarginLeft(1))) { tag ->
                when (tag) {
                    null -> textBlock("filter by tag", modify(Italic))
                    else -> textBlock(tag.label)
                }
            }
        }
        spacer(Flex1)
//        button(SvgFile.GearSmall, mod = modify(buttonMod, IconStyle.DefaultMod)) {
//            setPopoverTarget(EarthStyle.ConfigMenuId, true)
//        }
        button(SvgFile.CurrentLocation, model::panToCurrentLocation, iconButtonMod)
        searchField(model.searchTextState, modify(PointerEventsAuto, BlurBackdrop, EarthStyle.MoveDimmer))
    }

    val rowMod = modify(PointerEventsAuto, FlexWrap, Padding(1))
    popover(EarthStyle.ConfigMenuId) {
        row(rowMod) {
            textBlock("coming soon")
        }
    }

    popover(EarthStyle.TagMenuId) { popover ->
        fun setTag(tag: EventTag?) {
            popover.close()
            model.setTag(tag)
        }

        flowBlock(model.tagState) { stateTag ->
            row(rowMod) {
                button("All", { setTag(null) }, modify(modify(if (stateTag != null) Zen else null)))
                EventTag.entries.forEach { tag ->
                    button(tag.label, { setTag(tag) }, modify(if (tag != stateTag) Zen else null))
                }
            }
        }
    }
}