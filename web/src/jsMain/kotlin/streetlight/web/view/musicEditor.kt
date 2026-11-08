package streetlight.web.view

import kampfire.model.storeOf
import koala.SvgFile
import koala.component.IconStyle
import koala.component.heading5
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.OrderHandle
import koala.dom.markdown.textEditor
import koala.dom.layout.flowBlock
import koala.dom.layout.orderColumn
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import kampfire.model.mutableTapOf
import kampfire.model.tapOf
import kotlinx.coroutines.launch
import kotlinx.css.Flex
import kotlinx.css.pct
import kotlinx.css.px
import streetlight.model.music.Beats
import kotlinx.css.LinearDimension
import streetlight.model.music.ChordTrack
import streetlight.model.music.DrumTrack
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.text.NotationError
import streetlight.model.music.ui.SectionNotation
import streetlight.model.music.ui.lines
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.layout.menuActionsRow
import streetlight.web.dom.layout.actionSection
import streetlight.web.model.ChordEditor
import streetlight.web.model.DrumEditor
import streetlight.web.model.LyricEditor
import streetlight.web.model.MelodyEditor
import kampfire.model.MutableTap
import kampfire.model.Tap
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicStudio
import streetlight.web.model.NotationView
import streetlight.web.model.TrackPlayer
import streetlight.web.model.SectionMic
import streetlight.web.model.SectionClipEditor
import streetlight.web.model.getSectionClipEditor
import streetlight.web.model.SectionMicPhase
import streetlight.web.model.getSectionMic
import kotlinx.html.span
import streetlight.web.model.exportMidi
import kotlin.uuid.Uuid

/** Renders the song's music, the style it is written in and its tracks. */
context(studio: MusicStudio, model: MusicEditor, player: TrackPlayer)
fun ViewScope.musicEditor() = column {
    row {
        dropMenu(studio.notationStyleState)
        dropMenu(studio.notationViewState)
    }
    with(model.properties) {
        notationPanes("music", textState, errorsState) {
            flowBlock(notationState) { notation ->
                textBlock(notation, modify(MonoFont, WhiteSpacePreLine))
            }
        }
    }
    trackList()
}

context(studio: MusicStudio, model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackList() {
    val newId = Id(Uuid.random().toString())
    val exportId = Id(Uuid.random().toString())
    popover(exportId) { popover ->
        actionMenu(listOf(
            MenuAction("MIDI") {
                exportMidi(model.storedState.now)
                popover.close()
            },
        ))
    }
    popover(newId) { popover ->
        actionMenu(listOf(
            MenuAction("Chords") {
                model.addTrack(ChordTrack(model.newTrackId("Chords"), emptyList(), emptyList()))
                popover.close()
            },
            MenuAction("Melody") {
                model.addTrack(MelodyTrack(model.newTrackId("Melody"), emptyList(), emptyList()))
                popover.close()
            },
            MenuAction("Lyrics") {
                model.addTrack(LyricTrack(model.newTrackId("Lyrics"), emptyList(), emptyList()))
                popover.close()
            },
            MenuAction("Drums") {
                model.addTrack(DrumTrack(model.newTrackId("Drums"), emptyList(), emptyList()))
                popover.close()
            },
        ))
    }
    actionSection(
        label = "Tracks",
        actions = listOf(
            MenuAction("Play") { contentScope.launch { player.play(model.storedState.now, takes = model.readTakes()) } },
            MenuAction("Stop") { player.stop() },
            MenuAction("Export", config = { setPopoverTarget(exportId, true) }) { },
            MenuAction("New", config = { setPopoverTarget(newId, true) }) { },
        ),
    ) {
        // keyed by name, so an edit to a track's content or a move leaves its open editor in place
        orderColumn(model.tracksState, { it.trackId }) { track -> trackRow(track) }
        flowBlock(model.tracksState.tapOf { it.isEmpty() }) { isEmpty ->
            if (isEmpty) textBlock("No tracks yet.", modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
        }
    }
}

/**
 * Renders the row of [track], which opens its editor below it or closes it, with a grip that moves it among the
 * tracks.
 */
context(handle: OrderHandle, studio: MusicStudio, model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackRow(track: Track) {
    val trackId = track.trackId
    val nameId = Id(Uuid.random().toString())
    trackNamePopover(nameId, trackId)
    row(modify(AlignItemsCenter, ZenBg, Padding(1))) {
        handle.grip(icon(SvgFile.GripVertical.small, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Move"))))
        button({ model.toggleTrack(trackId) }, Flex1) {
            row(AlignItemsCenter) {
                icon(track.svg.large, Height(5))
                heading5(trackId.name)
            }
        }
        // a lyric track makes no sound, so it has nothing to play
        val play = MenuAction("Play") {
            if (player.isPlayingState.now) player.stop()
            else model.readTrack(trackId)?.let { track ->
                contentScope.launch { player.play(model.storedState.now, track, takes = model.readTakes()) }
            }
        }.takeIf { track !is LyricTrack }
        menuActionsRow(listOfNotNull(
            play,
            MenuAction("Edit", config = { setPopoverTarget(nameId, true) }) { },
            MenuAction("Remove") { model.removeTrack(trackId) },
        ))
    }
    flowBlock(model.isTrackOpenState(trackId)) { isOpen ->
        if (isOpen) trackEditor(trackId)
    }
}

/** A popover with [id] holding a field for the name of the track [trackId]. */
context(model: MusicEditor)
private fun ViewScope.trackNamePopover(id: Id, trackId: TrackId) {
    popover(id) { popover ->
        val nameState = storeOf(trackId.name)
        val save = {
            model.renameTrack(trackId, nameState.now)
            popover.close()
        }
        row(modify(AlignItemsCenter, Padding(2))) {
            textField(nameState, "name", Flex1, onEnterSubmit = save)
            button(SvgFile.Check.small, save, Height(5))
        }
    }
}

/** Renders the editor of the track [trackId]. */
context(studio: MusicStudio, model: MusicEditor)
private fun ViewScope.trackEditor(trackId: TrackId) {
    val track = model.readTrack(trackId) ?: return
    column(PaddingY(1)) {
        when (track) {
            is ChordTrack -> with(ChordEditor(contentScope, model, trackId)) {
                notationPanes("chords", textState, errorsState, studio.notationViewState) { sectionList(trackId, sectionsState) }
            }
            is MelodyTrack -> with(MelodyEditor(contentScope, model, trackId)) {
                notationPanes("jianpu", textState, errorsState, studio.notationViewState) { sectionList(trackId, sectionsState) }
            }
            is LyricTrack -> with(LyricEditor(contentScope, model, trackId)) {
                notationPanes("lyrics", textState, errorsState, studio.notationViewState) { sectionList(trackId, sectionsState) }
            }
            is DrumTrack -> with(DrumEditor(contentScope, model, trackId)) {
                notationPanes("drums", textState, errorsState, studio.notationViewState) { sectionList(trackId, sectionsState) }
            }
        }
        flowBlock(model.sectionWarningsState(trackId)) { warnings ->
            warnings.forEach { textBlock("Warning: $it", modify(TextSmall, InkDimFg)) }
        }
    }
}

/**
 * Renders the text in [textState], then [output], what the text reads as, then its [errorsState], each part as
 * [viewState] shows it. The errors go with the text. Showing both, a wide pane puts the notes beside the text.
 */
private fun ViewScope.notationPanes(
    label: String,
    textState: MutableTap<String>,
    errorsState: Tap<List<NotationError>>,
    viewState: Tap<NotationView> = storeOf(NotationView.Both),
    output: ViewScope.() -> Unit,
) {
    flowBlock(viewState, StudioStyle.Panes) { view ->
        // a lone part keeps the whole width, so only both parts split
        val split = StudioStyle.SplitPanes.takeIf { view == NotationView.Both }
        div(modify(StudioStyle.PanesGrid, split)) {
            if (view != NotationView.NotesOnly) {
                textEditor(textState, label, modify(MonoFont, StudioStyle.TextPane), spellcheck = false)
            }
            if (view != NotationView.TextOnly) div(StudioStyle.NotesPane) { output() }
            if (view != NotationView.NotesOnly) {
                flowBlock(errorsState, StudioStyle.ErrorsPane) { errors ->
                    errors.forEach { textBlock("Line ${it.line}: ${it.message}", modify(TextSmall, InkDimFg)) }
                }
            }
        }
    }
}

/**
 * Renders each section of [sectionsState] of the track [trackId] under its name, with the action that records a take
 * of it, a strip for each line of its text. A measure takes the same width on every line, so a short line stays
 * short, and a beat no more than [MAX_BEAT_UNITS] units.
 */
context(model: MusicEditor)
private fun ViewScope.sectionList(trackId: TrackId, sectionsState: Tap<List<SectionNotation>>) = flowBlock(sectionsState, FlexColumn) { sections ->
    sections.forEach { section ->
        val clipId = Id(Uuid.random().toString())
        val recordId = Id(Uuid.random().toString())
        sectionClipPopover(clipId, app.getSectionClipEditor(contentScope, model, trackId, section.sectionId))
        sectionMicPopover(recordId, app.getSectionMic(contentScope, model, trackId, section.sectionId))
        actionSection(
            label = section.sectionId.name,
            actions = listOf(
                MenuAction("Clip", config = { setPopoverTarget(clipId, true) }) { },
                MenuAction("Record", config = { setPopoverTarget(recordId, true) }) { },
            ),
            heading = { span { +it } },
        ) {
            sectionStrips(section)
        }
    }
}

/** The strips of [section], a line of its text to each. */
private fun ViewScope.sectionStrips(section: SectionNotation) {
    val lines = section.lines()
    // the pickup and staff hang off the left of each strip, so a margin as wide as the widest keeps them in the pane
    val leadTicks = lines.maxOfOrNull { maxOf(it.pickupShare, it.staffLeadShare) * it.ticks } ?: 0.0
    val paneTicks = section.measuresPerLine * section.measureTicks + leadTicks
    // a line of few measures would stretch across a wide pane, so a beat takes no more than its widest
    val maxWidth = LinearDimension("calc(var(--unit) * ${paneTicks / Beats.TICKS_PER_BEAT * MAX_BEAT_UNITS})")
    // the first head and its sharp reach left of their onset by a fixed width, which no margin of ticks covers
    val headRoom = LinearDimension("calc(var(--unit) * $HEAD_ROOM_UNITS)")
    column(modify(Css.MaxWidth.of(maxWidth), Css.Gap.of(2.px))) {
        lines.forEach { line ->
            val place = modify(
                Css.MarginLeft.of((leadTicks / paneTicks * 100).pct),
                Css.Width.of((line.ticks / paneTicks * 100).pct),
            )
            // each line on a band of its own, the band reaching the whole width however short the line
            div(modify(ZenBg, Css.PaddingLeft.of(headRoom))) { sectionMeasures(line, place) }
        }
    }
}

/** A popover with [id] holding the choice of the clip [clipEditor] binds to its section, or none. */
private fun ViewScope.sectionClipPopover(id: Id, clipEditor: SectionClipEditor) {
    popover(id) {
        flowBlock(clipEditor.namesState, modify(Padding(2), MinWidth(48))) { names ->
            dropMenu(names, clipEditor.selectedNameState, Flex1)
        }
    }
}

/**
 * A popover with [id] that records takes through [mic]: the lead-in's progress beside the take's, then the name
 * the take is kept under and the button that starts it, or stops it early and discards it.
 */
private fun ViewScope.sectionMicPopover(id: Id, mic: SectionMic) {
    popover(id) {
        column(modify(Padding(2), MinWidth(64))) {
            row(AlignItemsCenter) {
                progressBar(mic.leadProgressState, modify(Flex1, Height(2), ZenBg), isSmooth = false)
                progressBar(mic.takeProgressState, modify(Css.Flex.of(Flex(4)), Height(2), ZenBg), isSmooth = false)
            }
            row(AlignItemsCenter) {
                textField(mic.nameState, "name", Flex1)
                flowBlock(mic.phaseState) { phase ->
                    val label = when (phase) {
                        SectionMicPhase.Idle -> "Record"
                        SectionMicPhase.LeadIn, SectionMicPhase.Recording -> "Stop"
                        SectionMicPhase.Saving -> "Saving"
                    }
                    button(label, { mic.toggle() })
                }
            }
            switch("Click", mic.isClickOnState)
            flowBlock(mic.isCalibratedState) { isCalibrated ->
                if (!isCalibrated) textBlock("Calibrate in the Studio so takes land on the beat.", modify(TextSmall, InkDimFg))
            }
        }
    }
}

/** The icon of the track's type. */
private val Track.svg get() = when (this) {
    is ChordTrack -> SvgFile.AcousticGuitar
    is MelodyTrack -> SvgFile.Music
    is LyricTrack -> SvgFile.Microphone
    is DrumTrack -> SvgFile.Drum
}

/** The widest a beat of a section's notation is drawn, in units. */
private const val MAX_BEAT_UNITS = 6

/** The room left of a section's notation for half a note's head and a sharp before it, in units. */
private const val HEAD_ROOM_UNITS = 2.4
