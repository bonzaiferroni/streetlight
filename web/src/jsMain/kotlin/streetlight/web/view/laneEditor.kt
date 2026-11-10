@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.view

import koala.SvgFile
import streetlight.model.music.TrackId
import koala.component.IconStyle
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.launchEffect
import koala.dom.requireClosest
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import kotlinx.css.LinearDimension
import kotlinx.css.pct
import streetlight.model.music.ui.NoteNotation
import streetlight.model.music.ui.NoteValue
import streetlight.web.dom.layout.actionMenu
import streetlight.web.model.Lane
import streetlight.web.model.LaneEditor
import streetlight.web.model.LaneSection
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
    column(modify(Zen, BorderRadius1, OverflowClip)) {
        studioTimeline(LaneStyle.Row)
        div(modify(FlexColumn, Gap2Px, StudioStyle.Stage)) {
            flowBlock(model.lanesState, FlexColumn) { lanes ->
                lanes.forEachIndexed { index, lane -> lane(index, lane) }
            }
            cursorOverlay(LaneStyle.CursorOverlay)
        }
    }
}

context(model: LaneEditor)
private fun ViewScope.laneControls() = row(modify(AlignSelfCenter, AlignItemsCenter)) {
    button(SvgFile.PlayerPlay.filled, { model.play() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")))
    button(SvgFile.PlayerStop.filled, { model.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    timeStepControl()
    switch("scale", model.isScaleSnapState)
    switch("lyrics", model.isLyricsShownState)
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
            div(modify(MeasureStyle.Note, MeasureStyle.ControlNote, step.shape)) {
                div(MeasureStyle.Head)
            }
        }
    }
}

/** Renders the toggles of the track [trackId]'s lane: its tones in place of its takes, its mute and its solo. */
context(model: LaneEditor)
private fun ViewScope.laneToggles(trackId: TrackId) = div(LaneStyle.Toggles) {
    val iconMod = modify(IconStyle.DefaultMod, Height(3))
    iconToggle("Tones", model.isTonedState(trackId), SvgFile.Music.small, SvgFile.Microphone.small,
        { model.toggleTones(trackId) }, iconMod = iconMod)
    iconToggle("Mute", model.isMutedState(trackId), SvgFile.VolumeOff.small, SvgFile.Volume.small,
        { model.toggleMute(trackId) }, iconMod = iconMod)
    iconToggle("Solo", model.isSoloedState(trackId), SvgFile.Headphones.filled, SvgFile.Headphones.small,
        { model.toggleSolo(trackId) }, iconMod = iconMod)
}

/**
 * Renders [lane], the one at [laneIndex], as its meta panel and each of its sections, placed within the window, under
 * the note cursor of a drag.
 */
context(model: LaneEditor)
private fun ViewScope.lane(laneIndex: Int, lane: Lane) {
    lateinit var leadElement: HTMLElement
    lateinit var contentElement: HTMLElement
    val sections = mutableListOf<Pair<LaneSection, HTMLElement>>()
    val rowElement = div(LaneStyle.Row) {
        div(LaneStyle.Meta) {
            textBlock(lane.label)
            laneToggles(lane.trackId)
        }
        contentElement = div(modify(Css.Height.of(laneHeight(lane)), LaneStyle.Content, LaneStyle.Lane, CardBg)) {
            leadElement = div(modify(VoidBg, LaneStyle.LeadIn))
            lane.sections.forEach { section ->
                sections += section to div(modify(LaneStyle.Section, LaneStyle.RepeatSection.takeIf { section.isRepeat })) {
                    textBlock(section.notation.name, modify(LaneStyle.SectionLabel, TextSmall))
                    sectionMeasures(section.notation, VoidBg) { note, noteElement ->
                        wireNoteDrag(laneIndex, section, note, noteElement)
                    }
                }
            }
            flowBlock(model.noteCursorState(laneIndex), LaneStyle.NoteCursor) { cursor ->
                cursor?.let { note(it) }
            }
        }
    }

    contentElement.addEventListener(PointerEvent.CLICK, { model.toggleLane(laneIndex) })

    launchEffect("lane") {
        model.isLaneSelectedState(laneIndex).flow.collect { isSelected ->
            if (isSelected) rowElement.modify(LaneStyle.Selected) else rowElement.unmodify(LaneStyle.Selected)
        }
    }

    launchEffect("lane") {
        model.windowState.flow.collect { window ->
            leadElement.placeSpan(0.0, lane.leadSeconds, window)
            sections.forEach { (section, element) -> element.placeSpan(section.startSeconds, section.durationSeconds, window) }
        }
    }
}

/**
 * Lets [noteElement] be dragged across its strip to edit [note] of [section], while its lane is selected. A release
 * back over the note cancels the drag.
 */
context(model: LaneEditor)
private fun wireNoteDrag(
    laneIndex: Int,
    section: LaneSection,
    note: NoteNotation,
    noteElement: HTMLElement,
) {
    var grabY = 0
    var isGrabbed = false

    noteElement.addEventListener(PointerEvent.POINTER_DOWN, { event ->
        if (!model.startNoteDrag(laneIndex, section, note)) return@addEventListener
        event.stopPropagation()
        noteElement.setPointerCapture(event.pointerId)
        grabY = event.clientY
        isGrabbed = true
    })
    noteElement.addEventListener(PointerEvent.POINTER_MOVE, { event ->
        if (!noteElement.hasPointerCapture(event.pointerId)) return@addEventListener
        val strip = noteElement.requireClosest(MeasureStyle.Strip).getBoundingClientRect()
        if (strip.width == 0.0 || strip.height == 0.0) return@addEventListener
        val tick = (event.clientX - strip.left) / strip.width * section.notation.ticks
        val stepsUp = (grabY - event.clientY) / (strip.height / section.notation.steps)
        model.dragNote(tick, stepsUp)
    })
    noteElement.addEventListener(PointerEvent.POINTER_UP, { event ->
        val head = noteElement.getBoundingClientRect()
        val isOverNote = event.clientX >= head.left && event.clientX <= head.right &&
            event.clientY >= head.top && event.clientY <= head.bottom
        model.endNoteDrag(isCommitted = !isOverNote)
    })
    noteElement.addEventListener(PointerEvent.POINTER_CANCEL, { model.endNoteDrag(isCommitted = false) })

    // the click that ends a drag is not a tap on the lane, which would clear its selection
    noteElement.addEventListener(PointerEvent.CLICK, { event ->
        if (isGrabbed) event.stopPropagation()
        isGrabbed = false
    })
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
