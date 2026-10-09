package streetlight.web.ui

import koala.SvgFile
import koala.dom.*
import koala.html.IconStyle
import koala.html.spacer
import koala.modifier.*
import streetlight.model.data.EventTag
import streetlight.web.model.Earth

fun ViewScope.earthControls(model: Earth) {
    val buttonMod = modify(PointerEventsAuto, OpacityHigh, EarthStyle.MoveDimmer)
    val iconButtonMod = modify(buttonMod, IconStyle.DefaultMod)
    val buttonTextMod = modify(TextSmall, TextUppercase)

    column(Padding(1)) {
        row(AlignItemsCenter) {
            button(mod = buttonMod) {
                setPopoverTarget(EarthStyle.TagMenuId, true)
                row(modify(AlignItemsCenter, Gap0)) {
                    icon(SvgFile.Label.small)
                    flowBlock(model.tagState, modify(MarginLeft(1))) { tag ->
                        when (tag) {
                            null -> textBlock("filter by type", buttonTextMod)
                            else -> textBlock(tag.label, buttonTextMod)
                        }
                    }
                }
            }
            spacer(Flex1)
            button(SvgFile.Gear.small, mod = iconButtonMod) {
                setPopoverTarget(EarthStyle.ConfigMenuId, true)
            }
            searchField(model.searchTextState, modify(PointerEventsAuto, BlurBackdrop, EarthStyle.MoveDimmer))
        }
        row(AlignItemsCenter) {
            spacer(Flex1)
            button(model::locateStar, buttonMod) {
                row(AlignItemsCenter) {
                    textBlock("find me", buttonTextMod)
                    icon(SvgFile.CurrentLocation.small).flowModifier(model.isLocatingState, SpinLoop, contentScope)
                }
            }
        }
    }

    val rowMod = modify(PointerEventsAuto, FlexWrap, Padding(1))
    popover(EarthStyle.ConfigMenuId) {
        row(rowMod) {
            switch("track my location", model.trackLocationState)
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