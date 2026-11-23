@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.view

import koala.SvgFile
import koala.component.IconStyle
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.css.pct
import streetlight.model.music.ui.NoteValue
import streetlight.web.dom.layout.actionMenu
import streetlight.web.model.Lane
import streetlight.web.model.LaneEditor
import streetlight.web.model.MusicConfig
import streetlight.web.model.LaneSection
import streetlight.web.model.MusicHost
import streetlight.web.model.TimeWindow
import streetlight.web.model.MusicPlayer
import streetlight.web.model.WaveShelf
import web.html.HTMLElement
import kotlin.uuid.Uuid

/** Renders the song's lanes below their player controls and timeline. */
context(config: MusicConfig, host: MusicHost, waveShelf: WaveShelf, player: MusicPlayer)
fun ViewScope.laneEditor() {
    val model = LaneEditor(contentScope, host, waveShelf, player)
    context(model) {
        column {
            laneControls()
            column(modify(Zen, BorderRadius1, OverflowClip)) {
                laneTimeline(LaneStyle.Row)
                div(modify(FlexColumn, Gap2Px, StudioStyle.Stage)) {
                    flowBlock(model.lanesState, FlexColumn) { lanes ->
                        lanes.forEachIndexed { index, lane -> lane(index, lane) }
                    }
                    cursorOverlay(LaneStyle.CursorOverlay)
                }
            }
        }
    }
}

context(config: MusicConfig, model: LaneEditor)
private fun ViewScope.laneControls() = row(modify(AlignSelfCenter, AlignItemsCenter)) {
    button(SvgFile.PlayerPlay.filled, { model.play() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")))
    button(SvgFile.PlayerStop.filled, { model.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    timeStepControl()
    // switch("scale", model.isScaleSnapState)
    switch("lyrics", config.isLyricsShownState)
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

/**
 * Renders [lane], the one at [laneIndex], as its meta panel and each of its sections, placed within the window, under
 * the note cursor of a drag.
 */
context(config: MusicConfig, model: LaneEditor)
private fun ViewScope.lane(laneIndex: Int, lane: Lane) {
    lateinit var leadElement: HTMLElement
    lateinit var contentElement: HTMLElement
    val sections = mutableListOf<Pair<LaneSection, HTMLElement>>()
    val rowElement = div(LaneStyle.Row) {
        div(LaneStyle.Meta) {
            textBlock(lane.label)
            // laneToggles(lane.trackId)
        }
        contentElement = div(modify(LaneStyle.Content, LaneStyle.Lane, CardBg)) {
            leadElement = div(modify(VoidBg, LaneStyle.LeadIn))
            lane.sections.forEach { section ->
                sections += section to div(modify(LaneStyle.Section, LaneStyle.RepeatSection.takeIf { section.isRepeat })) {
                    textBlock(section.notation.name, modify(LaneStyle.SectionLabel, TextSmall))
                    notationMeasures(section.notation, VoidBg) { note, noteElement ->
                        // wireNoteDrag(laneIndex, section, note, noteElement)
                    }
                }
            }
//            flowBlock(model.noteCursorState(laneIndex), LaneStyle.NoteCursor) { cursor ->
//                cursor?.let { note(it) }
//            }
        }
    }

    // contentElement.addEventListener(PointerEvent.CLICK, { model.toggleLane(laneIndex) })

    launchEffect("lane") {
//        model.isLaneSelectedState(laneIndex).flow.collect { isSelected ->
//            if (isSelected) rowElement.modify(LaneStyle.Selected) else rowElement.unmodify(LaneStyle.Selected)
//        }
    }

    launchEffect("lane") {
        model.windowState.flow.collect { window ->
            leadElement.placeSpan(0.0, lane.leadSeconds, window)
            sections.forEach { (section, element) -> element.placeSpan(section.startSeconds, section.durationSeconds, window) }
        }
    }
}

/** Renders the cursor as one line across every lane, in line with the cursor's place in the window, over [overlayMod]. */
context(model: LaneEditor)
fun ViewScope.cursorOverlay(overlayMod: Modifier = StudioStyle.CursorOverlay) {
    lateinit var lineElement: HTMLElement
    div(overlayMod) {
        lineElement = div(StudioStyle.CursorLine)
    }
    placeAt(lineElement, readCursorRatioFlow())
}

/** Keeps [element] at each ratio of [ratioFlow] along its container. */
private fun ViewScope.placeAt(element: HTMLElement, ratioFlow: Flow<Double>) {
    launchEffect("placeAt") {
        ratioFlow.collect { element.setStyle(Css.Left.of((it * 100).pct)) }
    }
}

/** The cursor's position across the window, as it moves. */
context(model: LaneEditor)
fun readCursorRatioFlow() =
    combine(model.cursorSecondsState.flow, model.windowState.flow) { cursor, window -> window.toRatio(cursor) }

/** Places this element across [window], [durationSeconds] from [startSeconds], running past its edges to keep its scale. */
private fun HTMLElement.placeSpan(startSeconds: Double, durationSeconds: Double, window: TimeWindow) {
    setStyle(Css.Left.of((window.toRatio(startSeconds) * 100).pct))
    setStyle(Css.Width.of((durationSeconds / window.spanSeconds * 100).pct))
}

/** The grids a dragged note can snap to. */
private val TIME_STEPS = listOf(NoteValue.Half, NoteValue.Quarter, NoteValue.Eighth, NoteValue.Sixteenth)
