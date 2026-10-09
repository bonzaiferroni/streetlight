package streetlight.web.ui

import koala.SvgFile
import koala.dom.*
import koala.modifier.*
import streetlight.model.data.EventTag
import streetlight.model.ui.EventsMapRoute
import streetlight.web.model.Earth
import web.html.HTMLDivElement

fun ViewScope.earthControls(model: Earth) {
    row(modify(JustifyContentSpaceBetween, AlignItemsCenter, Padding(1))) {
        button(mod = modify(PointerEventsAuto, OpacityHigh, Css.AnchorName.of(EarthStyle.TagMenuId.toPositionAnchor()))) {
            setPopoverTarget(EarthStyle.TagMenuId)
            icon(SvgFile.Label)
            flowBlock(model.tagState, modify(MarginLeft(1))) { tag ->
                when (tag) {
                    null -> textBlock("filter by tag", modify(Italic))
                    else -> textBlock(tag.label)
                }
            }
        }
        searchField(model.searchTextState, modify(PointerEventsAuto, BlurBackdrop))
    }

    popover(EarthStyle.TagMenuId) { popover ->
        fun setTag(tag: EventTag?) {
            popover.close()
            portal.go(EventsMapRoute(null, tag))
        }

        flowBlock(model.tagState) { stateTag ->
            row(modify(PointerEventsAuto, FlexWrap, Padding(1))) {
                button("All", { setTag(null) }, modify(modify(if (stateTag != null) Zen else null)))
                EventTag.entries.forEach { tag ->
                    button(tag.label, { setTag(tag) }, modify(if (tag != stateTag) Zen else null))
                }
            }
        }
    }
}