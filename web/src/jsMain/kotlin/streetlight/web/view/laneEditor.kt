@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.view

import koala.SvgFile
import koala.component.IconStyle
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.launchEffect
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import kotlinx.css.LinearDimension
import kotlinx.css.pct
import streetlight.web.dom.layout.actionMenu
import streetlight.web.model.Lane
import streetlight.web.model.LaneEditor
import streetlight.web.model.LaneSlot
import streetlight.web.model.NoteNotation
import streetlight.web.model.NoteValue
import streetlight.web.model.TimeWindow
import web.events.addEventListener
import web.html.HTMLElement
import web.pointer.CLICK
import web.pointer.POINTER_CANCEL
import web.pointer.POINTER_DOWN
import web.pointer.POINTER_MOVE
import web.pointer.POINTER_UP
import web.pointer.PointerEvent
import kotlin.uuid.Uuid

/** Renders the song's lanes below their player controls and timeline. */
context(model: LaneEditor)
fun ViewScope.laneEditor() = column {
    laneControls()
    column(modify(Gap2Px, Zen, BorderRadius1, OverflowClip)) {
        studioTimeline(LaneStyle.Row)
        div(modify(FlexColumn, Gap2Px, StudioStyle.Stage)) {
            flowBlock(model.lanesState, modify(FlexColumn, Gap2Px)) { lanes ->
                lanes.forEachIndexed { index, lane -> lane(index, lane) }
            }
            cursorOverlay(LaneStyle.CursorOverlay)
        }
    }
}

context(model: LaneEditor)
private fun ViewScope.laneControls() = row(AlignSelfCenter) {
    button(SvgFile.PlayerPlay.filled, { model.play() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")))
    button(SvgFile.PlayerStop.filled, { model.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    timeStepControl()
    switch("scale", model.isScaleSnapState)
}

/** Renders the button that shows the grid dragged notes snap to, drawn as its note, and opens a menu of the others. */
context(model: LaneEditor)
private fun ViewScope.timeStepControl() {
    val menuId = Id(Uuid.random().toString())
    popover(menuId) { popover ->
        actionMenu(TIME_STEPS.map { step ->
            MenuAction(step.name) {
                model.timeStepState.set(step)
                popover.close()
            }
        })
    }
    flowBlock(model.timeStepState) { step ->
        button(mod = modify(Zen, Attribute.AriaLabel.to("Time step"))) {
            setPopoverTarget(menuId, true)
            div(modify(MeasureStyle.Note, MeasureStyle.ControlNote, step.shape))
        }
    }
}

/**
 * Renders [lane], the one at [laneIndex], as its meta panel and a slot for each time its track plays a section, placed
 * within the window, under the note cursor of a drag.
 */
context(model: LaneEditor)
private fun ViewScope.lane(laneIndex: Int, lane: Lane) {
    lateinit var leadElement: HTMLElement
    lateinit var contentElement: HTMLElement
    val slots = mutableListOf<Pair<LaneSlot, HTMLElement>>()
    val rowElement = div(modify(LaneStyle.Row, CardBg)) {
        textBlock(lane.label, modify(LaneStyle.Meta, Padding(1)))
        contentElement = div(modify(Css.Height.of(laneHeight(lane)), LaneStyle.Content)) {
            leadElement = div(modify(VoidBg, LaneStyle.LeadIn))
            lane.slots.forEach { slot ->
                slots += slot to div(modify(LaneStyle.Slot, LaneStyle.RepeatSlot.takeIf { slot.isRepeat })) {
                    textBlock(slot.notation.sectionId.name, modify(LaneStyle.SlotLabel, TextSmall))
                    sectionMeasures(slot.notation, VoidBg) { note, noteElement, stripElement ->
                        wireNoteDrag(laneIndex, slot, note, noteElement, stripElement)
                    }
                }
            }
            flowBlock(model.noteCursorState(laneIndex), LaneStyle.NoteCursor) { cursor ->
                cursor?.let { note(it) }
            }
        }
    }

    contentElement.addEventListener(PointerEvent.CLICK, { model.selectLane(laneIndex) })

    launchEffect("lane") {
        model.isLaneSelectedState(laneIndex).flow.collect { isSelected ->
            if (isSelected) rowElement.modify(LaneStyle.Selected) else rowElement.unmodify(LaneStyle.Selected)
        }
    }

    launchEffect("lane") {
        model.windowState.flow.collect { window ->
            leadElement.placeSpan(0.0, lane.leadSeconds, window)
            slots.forEach { (slot, element) -> element.placeSpan(slot.startSeconds, slot.durationSeconds, window) }
        }
    }
}

/**
 * Lets [noteElement] be dragged across [stripElement] to edit [note] of [slot], while its lane is selected. A release
 * back over the note cancels the drag.
 */
context(model: LaneEditor)
private fun wireNoteDrag(
    laneIndex: Int,
    slot: LaneSlot,
    note: NoteNotation,
    noteElement: HTMLElement,
    stripElement: HTMLElement,
) {
    var grabY = 0

    noteElement.addEventListener(PointerEvent.POINTER_DOWN, { event ->
        if (!model.startNoteDrag(laneIndex, slot, note)) return@addEventListener
        event.stopPropagation()
        noteElement.setPointerCapture(event.pointerId)
        grabY = event.clientY
    })
    noteElement.addEventListener(PointerEvent.POINTER_MOVE, { event ->
        if (!noteElement.hasPointerCapture(event.pointerId)) return@addEventListener
        val strip = stripElement.getBoundingClientRect()
        if (strip.width == 0.0 || strip.height == 0.0) return@addEventListener
        val tick = (event.clientX - strip.left) / strip.width * slot.notation.ticks
        val stepsUp = (grabY - event.clientY) / (strip.height / slot.notation.steps)
        model.dragNote(tick, stepsUp)
    })
    noteElement.addEventListener(PointerEvent.POINTER_UP, { event ->
        val head = noteElement.getBoundingClientRect()
        val isOverNote = event.clientX >= head.left && event.clientX <= head.right &&
            event.clientY >= head.top && event.clientY <= head.bottom
        model.endNoteDrag(isCommitted = !isOverNote)
    })
    noteElement.addEventListener(PointerEvent.POINTER_CANCEL, { model.endNoteDrag(isCommitted = false) })
}

/** The height of [lane]: its label's sublane and the staff steps of its notes. */
private fun laneHeight(lane: Lane) = LinearDimension("calc(var(--unit-3) + var(--unit) * ${STAFF_STEP_UNITS * lane.steps})")

/** Places this element across [window], [durationSeconds] from [startSeconds], running past its edges to keep its scale. */
private fun HTMLElement.placeSpan(startSeconds: Double, durationSeconds: Double, window: TimeWindow) {
    setStyle(Css.Left.of((window.toRatio(startSeconds) * 100).pct))
    setStyle(Css.Width.of((durationSeconds / window.spanSeconds * 100).pct))
}

/** The grids a dragged note can snap to. */
private val TIME_STEPS = listOf(NoteValue.Half, NoteValue.Quarter, NoteValue.Eighth, NoteValue.Sixteenth)
