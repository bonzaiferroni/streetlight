package streetlight.web.view

import koala.SvgFile
import koala.component.IconStyle
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.launchEffect
import koala.dom.layout.flowBlock
import koala.modifier.*
import kotlinx.css.pct
import streetlight.web.model.Lane
import streetlight.web.model.LaneEditor
import streetlight.web.model.LaneSlot
import streetlight.web.model.TimeWindow
import web.html.HTMLElement

/** Renders the song's lanes below their player controls and timeline. */
context(model: LaneEditor)
fun ViewScope.laneEditor() = column {
    laneControls()
    column(modify(Gap2Px, Zen, BorderRadius1, OverflowClip)) {
        studioTimeline(LaneStyle.Row)
        div(modify(FlexColumn, Gap2Px, StudioStyle.Stage)) {
            flowBlock(model.lanesState, modify(FlexColumn, Gap2Px)) { lanes ->
                lanes.forEach { lane(it) }
            }
            cursorOverlay(LaneStyle.CursorOverlay)
        }
    }
}

context(model: LaneEditor)
private fun ViewScope.laneControls() = row(AlignSelfCenter) {
    button(SvgFile.PlayerPlay.filled, { model.play() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")))
    button(SvgFile.PlayerStop.filled, { model.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
}

/** Renders [lane] as its meta panel and a slot for each time its track plays a section, placed within the window. */
context(model: LaneEditor)
private fun ViewScope.lane(lane: Lane) {
    val slots = mutableListOf<Pair<LaneSlot, HTMLElement>>()
    div(modify(LaneStyle.Row, CardBg)) {
        textBlock(lane.label, Padding(1))
        div(modify(Height(12), LaneStyle.Content)) {
            lane.slots.forEach { slot ->
                val style = if (slot.isRepeat) LaneStyle.RepeatSlot else LaneStyle.InitialSlot
                slots += slot to div(modify(VoidBg, LaneStyle.Slot, style)) {
                    sectionMeasures(slot.notation, LaneStyle.SlotMeasures)
                    textBlock(slot.notation.sectionId.name, modify(Padding(1), TextSmall))
                }
            }
        }
    }

    launchEffect("lane") {
        model.windowState.flow.collect { window ->
            slots.forEach { (slot, element) -> element.placeSlot(slot, window) }
        }
    }
}

/** Places this element at the true span of [slot] across [window], running past its edges so its measures keep their scale. */
private fun HTMLElement.placeSlot(slot: LaneSlot, window: TimeWindow) {
    setStyle(Css.Left.of((window.toRatio(slot.startSeconds) * 100).pct))
    setStyle(Css.Width.of((slot.durationSeconds / window.spanSeconds * 100).pct))
}
