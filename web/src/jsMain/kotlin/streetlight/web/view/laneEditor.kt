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
    lateinit var leadElement: HTMLElement
    val slots = mutableListOf<Pair<LaneSlot, HTMLElement>>()
    div(modify(LaneStyle.Row, CardBg)) {
        textBlock(lane.label, Padding(1))
        div(modify(Css.Height.of(stepsHeight(lane.steps + LABEL_STEPS)), LaneStyle.Content)) {
            leadElement = div(modify(VoidBg, LaneStyle.LeadIn))
            lane.slots.forEach { slot ->
                slots += slot to div(modify(LaneStyle.Slot, LaneStyle.RepeatSlot.takeIf { slot.isRepeat })) {
                    textBlock(slot.notation.sectionId.name, modify(LaneStyle.SlotLabel, TextSmall))
                    sectionMeasures(slot.notation, VoidBg)
                }
            }
        }
    }

    launchEffect("lane") {
        model.windowState.flow.collect { window ->
            leadElement.placeSpan(0.0, lane.leadSeconds, window)
            slots.forEach { (slot, element) -> element.placeSpan(slot.startSeconds, slot.durationSeconds, window) }
        }
    }
}

/** Places this element across [window], [durationSeconds] from [startSeconds], running past its edges to keep its scale. */
private fun HTMLElement.placeSpan(startSeconds: Double, durationSeconds: Double, window: TimeWindow) {
    setStyle(Css.Left.of((window.toRatio(startSeconds) * 100).pct))
    setStyle(Css.Width.of((durationSeconds / window.spanSeconds * 100).pct))
}

/** The staff steps of height a slot keeps above its measures for its label. */
private const val LABEL_STEPS = 3
