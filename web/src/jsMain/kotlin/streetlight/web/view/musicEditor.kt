package streetlight.web.view

import kampfire.model.storeOf
import koala.SvgFile
import koala.component.IconStyle
import koala.SvgPack
import koala.component.heading5
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import kampfire.model.mutableTapOf
import streetlight.model.data.ChordTrack
import streetlight.model.data.MelodyTrack
import streetlight.model.data.DrumTrack
import streetlight.model.data.LyricTrack
import streetlight.model.data.Track
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.layout.menuActionsRow
import streetlight.web.dom.layout.actionSection
import streetlight.web.model.ChordEditor
import streetlight.web.model.DrumEditor
import streetlight.web.model.LyricEditor
import streetlight.web.model.MelodyEditor
import kampfire.model.MutableTap
import kampfire.model.Tap
import streetlight.model.data.NotationError
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicStudio
import streetlight.web.model.SectionNotation
import streetlight.web.model.TrackPlayer
import streetlight.web.model.exportMidi
import streetlight.web.model.label
import kotlin.uuid.Uuid

/** Renders the song's music, the style it is written in and its tracks. */
context(studio: MusicStudio, model: MusicEditor, player: TrackPlayer)
fun ViewScope.musicEditor() = column {
    dropMenu(studio.notationStyleState)
    with(model.properties) {
        notationPanes("music", textState, errorsState, rows = 4) {
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
                model.addTrack(ChordTrack(emptyList(), emptyList()))
                popover.close()
            },
            MenuAction("Melody") {
                model.addTrack(MelodyTrack(emptyList(), emptyList()))
                popover.close()
            },
            MenuAction("Lyrics") {
                model.addTrack(LyricTrack(emptyList(), emptyList()))
                popover.close()
            },
            MenuAction("Drums") {
                model.addTrack(DrumTrack(emptyList(), emptyList()))
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
        // the list follows the labels alone, so an edit to a track's content leaves its open editor in place
        flowBlock(model.trackLabelsState) { labels ->
            labels.forEachIndexed { index, label ->
                model.readTrack(index)?.let { trackRow(index, label, it.svg) }
            }
            if (labels.isEmpty()) {
                textBlock("No tracks yet.", modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
            }
        }
    }
}

/** Renders the row of the track at [index] shown as [svg] and [label], which opens its editor below it or closes it. */
context(model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackRow(index: Int, label: String, svg: SvgPack) {
    val nameId = Id(Uuid.random().toString())
    trackNamePopover(nameId, index, label)
    row(modify(AlignItemsCenter, ZenBg, Padding(1))) {
        button({ model.toggleTrack(index) }, Flex1) {
            row(AlignItemsCenter) {
                icon(svg.large, Height(5))
                heading5(label)
            }
        }
        menuActionsRow(listOf(
            MenuAction("Edit", config = { setPopoverTarget(nameId, true) }) { },
            MenuAction("Remove") { model.removeTrack(index) },
        ))
    }
    flowBlock(model.isTrackOpenState(index)) { isOpen ->
        if (isOpen) trackEditor(index)
    }
}

/** A popover with [id] holding a field for the name of the track at [index], starting from [label]. */
context(model: MusicEditor)
private fun ViewScope.trackNamePopover(id: Id, index: Int, label: String) {
    popover(id) { popover ->
        val nameState = storeOf(model.readTrack(index)?.name ?: label)
        val save = {
            model.renameTrack(index, nameState.now)
            popover.close()
        }
        row(modify(AlignItemsCenter, Padding(2))) {
            textField(nameState, "name", Flex1, onEnterSubmit = save)
            button(SvgFile.Check.small, save, Height(5))
        }
    }
}

/** Renders the editor of the track at [index]. */
context(model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackEditor(index: Int) {
    val track = model.readTrack(index) ?: return
    column {
        when (track) {
            is ChordTrack -> {
                trackPlayer(index)
                with(ChordEditor(contentScope, model, index)) {
                    notationPanes("chords", textState, errorsState) { sectionList(sectionsState) }
                }
            }
            is MelodyTrack -> {
                trackPlayer(index)
                with(MelodyEditor(contentScope, model, index)) {
                    notationPanes("jianpu", textState, errorsState) { sectionList(sectionsState) }
                }
            }
            is LyricTrack -> with(LyricEditor(contentScope, model, index)) {
                notationPanes("lyrics", textState, errorsState) { sectionList(sectionsState) }
            }
            is DrumTrack -> with(DrumEditor(contentScope, model, index)) {
                notationPanes("drums", textState, errorsState) { sectionList(sectionsState) }
            }
        }
        flowBlock(model.sectionWarningsState(index)) { warnings ->
            warnings.forEach { textBlock("Warning: $it", modify(TextSmall, InkDimFg)) }
        }
    }
}

/** Renders the button that plays the track at [index], or stops it while it plays. */
context(model: MusicEditor, player: TrackPlayer)
private fun ViewScope.trackPlayer(index: Int) = flowBlock(player.isPlayingState) { isPlaying ->
    if (isPlaying) {
        button(SvgFile.PlayerStop.filled, { player.stop() }, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Stop")))
    } else {
        button(
            SvgFile.PlayerPlay.filled,
            { model.readTrack(index)?.let { player.play(model.storedState.now, it) } },
            modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Play")),
        )
    }
}

/** Renders the text in [textState] and its [errorsState] beside [output], what the text reads as. */
private fun ViewScope.notationPanes(
    label: String,
    textState: MutableTap<String>,
    errorsState: Tap<List<NotationError>>,
    rows: Int = 16,
    output: ViewScope.() -> Unit,
) {
    row(AlignItemsStart) {
        column(Flex1) {
            textEditor(textState, label, rows = rows, textMod = MonoFont)
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
        sectionMeasures(section, Height(8))
    }
}

/** The icon of the track's type. */
private val Track.svg get() = when (this) {
    is ChordTrack -> SvgFile.AcousticGuitar
    is MelodyTrack -> SvgFile.Music
    is LyricTrack -> SvgFile.Microphone
    is DrumTrack -> SvgFile.Drum
}
