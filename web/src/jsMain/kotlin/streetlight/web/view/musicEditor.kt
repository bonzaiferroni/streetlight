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
import kotlinx.css.pct
import streetlight.model.music.ChordTrack
import streetlight.model.music.DrumTrack
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.text.NotationError
import streetlight.model.music.ui.SectionNotation
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
import streetlight.web.model.TrackPlayer
import streetlight.web.model.exportMidi
import kotlin.uuid.Uuid

/** Renders the song's music, the style it is written in and its tracks. */
context(studio: MusicStudio, model: MusicEditor, player: TrackPlayer)
fun ViewScope.musicEditor() = column {
    dropMenu(studio.notationStyleState)
    with(model.properties) {
        notationPanes("music", textState, errorsState) {
            flowBlock(notationState) { notation ->
                textBlock(notation, modify(MonoFont, WhiteSpacePreLine))
            }
        }
    }
    trackList()
}

context(model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackList() {
    val newId = Id(Uuid.random().toString())
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
            MenuAction("Play") { player.play(model.storedState.now) },
            MenuAction("Stop") { player.stop() },
            MenuAction("Export") { exportMidi(model.storedState.now) },
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
context(handle: OrderHandle, model: MusicEditor, player: TrackPlayer)
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
        menuActionsRow(listOf(
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
context(model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackEditor(trackId: TrackId) {
    val track = model.readTrack(trackId) ?: return
    column {
        when (track) {
            is ChordTrack -> {
                trackPlayer(trackId)
                with(ChordEditor(contentScope, model, trackId)) {
                    notationPanes("chords", textState, errorsState) { sectionList(sectionsState) }
                }
            }
            is MelodyTrack -> {
                trackPlayer(trackId)
                with(MelodyEditor(contentScope, model, trackId)) {
                    notationPanes("jianpu", textState, errorsState) { sectionList(sectionsState) }
                }
            }
            is LyricTrack -> with(LyricEditor(contentScope, model, trackId)) {
                notationPanes("lyrics", textState, errorsState) { sectionList(sectionsState) }
            }
            is DrumTrack -> with(DrumEditor(contentScope, model, trackId)) {
                notationPanes("drums", textState, errorsState) { sectionList(sectionsState) }
            }
        }
        flowBlock(model.sectionWarningsState(trackId)) { warnings ->
            warnings.forEach { textBlock("Warning: $it", modify(TextSmall, InkDimFg)) }
        }
    }
}

/** Renders the button that plays the track [trackId], or stops it while it plays. */
context(model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackPlayer(trackId: TrackId) = flowBlock(player.isPlayingState) { isPlaying ->
    if (isPlaying) {
        button(SvgFile.PlayerStop.filled, { player.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    } else {
        button(
            SvgFile.PlayerPlay.filled,
            { model.readTrack(trackId)?.let { player.play(model.storedState.now, it) } },
            modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")),
        )
    }
}

/** Renders the text in [textState] and its [errorsState] beside [output], what the text reads as. */
private fun ViewScope.notationPanes(
    label: String,
    textState: MutableTap<String>,
    errorsState: Tap<List<NotationError>>,
    output: ViewScope.() -> Unit,
) {
    row(AlignItemsStart) {
        column(Flex1) {
            textEditor(textState, label, MonoFont, spellcheck = false)
            flowBlock(errorsState) { errors ->
                errors.forEach { textBlock("Line ${it.line}: ${it.message}", modify(TextSmall, InkDimFg)) }
            }
        }
        column(Flex1) { output() }
    }
}

/** Renders each section of [sectionsState] under its name. */
private fun ViewScope.sectionList(sectionsState: Tap<List<SectionNotation>>) = flowBlock(sectionsState, FlexColumn) { sections ->
    sections.forEach { section ->
        textBlock(section.sectionId.name, TextSmall)
        // the pickup and staff hang off the left of the strip, so a margin keeps them within the pane
        val leadShare = maxOf(section.pickupShare, section.staffLeadShare)
        val margin = leadShare / (1 + leadShare)
        sectionMeasures(section, Css.MarginLeft.of((margin * 100).pct))
    }
}

/** The icon of the track's type. */
private val Track.svg get() = when (this) {
    is ChordTrack -> SvgFile.AcousticGuitar
    is MelodyTrack -> SvgFile.Music
    is LyricTrack -> SvgFile.Microphone
    is DrumTrack -> SvgFile.Drum
}
