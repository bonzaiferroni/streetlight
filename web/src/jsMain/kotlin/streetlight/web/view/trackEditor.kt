package streetlight.web.view

import kampfire.model.mutableTapOf
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
import kampfire.model.tapOf
import kotlinx.coroutines.launch
import streetlight.model.music.ChordTrack
import streetlight.model.music.DrumTrack
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.text.NotationError
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.layout.menuActionsRow
import streetlight.web.dom.layout.actionSection
import kampfire.model.MutableTap
import kampfire.model.Tap
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicFormProblem
import streetlight.web.model.SectionEditor
import streetlight.web.model.readCapoText
import streetlight.model.music.Music
import streetlight.model.music.MusicSection
import streetlight.model.music.Phrase
import streetlight.model.music.UNNAMED_SECTION
import streetlight.web.dom.layout.actionSectionCard
import streetlight.web.model.MixEditor
import streetlight.web.model.MusicStudio
import streetlight.web.model.NotationView
import streetlight.web.model.TrackPlayer
import streetlight.web.model.exportMidi
import streetlight.web.model.exportOgg
import streetlight.web.model.toaster
import kampfire.model.toDataOr
import streetlight.model.music.SectionId
import kotlin.uuid.Uuid

/** Renders the tracks of the song under actions to play, export and add to them, each a row that opens its sections. */
context(studio: MusicStudio, model: MusicEditor, mix: MixEditor, player: TrackPlayer)
fun ViewScope.trackEditor() {
    val newId = Id(Uuid.random().toString())
    val exportId = Id(Uuid.random().toString())
    popover(exportId) { popover ->
        actionMenu(listOf(
            MenuAction("MIDI") {
                exportMidi(model.storedState.now)
                popover.close()
            },
            MenuAction("Ogg") {
                popover.close()
                contentScope.launch {
                    exportOgg(player, model.storedState.now, mix.readTakes()).toDataOr(toaster) { return@launch }
                }
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
            MenuAction("Play") { contentScope.launch { player.play(model.storedState.now, takes = mix.readTakes()) } },
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
context(handle: OrderHandle, studio: MusicStudio, model: MusicEditor, mix: MixEditor, player: TrackPlayer)
private fun ViewScope.trackRow(track: Track) {
    val trackId = track.trackId
    val nameId = Id(Uuid.random().toString())
    trackEditPopover(nameId, track)
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
                contentScope.launch { player.play(model.storedState.now, listOf(track), takes = mix.readTakes()) }
            }
        }.takeIf { track !is LyricTrack }
        menuActionsRow(listOfNotNull(
            play,
            MenuAction("Edit", config = { setPopoverTarget(nameId, true) }) { },
            MenuAction("Remove") { model.removeTrack(trackId) },
        ))
    }
    flowBlock(model.isTrackOpenState(trackId)) { isOpen ->
        if (isOpen) trackContent(trackId)
    }
}

/** A popover with [id] holding the name and capo of [track], which its save button applies. */
context(model: MusicEditor)
private fun ViewScope.trackEditPopover(id: Id, track: Track) {
    popover(id) { popover ->
        val nameState = storeOf(track.trackId.name)
        val capoTextState = storeOf(track.capo?.toString().orEmpty())
        val save = save@{
            val capo = readCapoText(capoTextState.now) ?: return@save toaster.deliver(MusicFormProblem.capo)
            model.setTrackCapo(track.trackId, capo.value)
            model.renameTrack(track.trackId, nameState.now)
            popover.close()
        }
        column(Padding(2)) {
            row(AlignItemsCenter) {
                textField(nameState, "name", Flex1, onEnterSubmit = save)
                textField(capoTextState, "capo", size = 4)
            }
            button("Save", save)
        }
    }
}

/**
 * Renders the content of the track [trackId]: its properties, with an action that adds a section, a card for each of
 * its sections, and its arrangements.
 */
context(studio: MusicStudio, model: MusicEditor)
private fun ViewScope.trackContent(trackId: TrackId) {
    val track = model.readTrack(trackId) ?: return
    val newId = Id(Uuid.random().toString())
    namePopover(newId, "Add") { model.addSection(trackId, it) }
    column(PaddingY(1)) {
        row(AlignItemsCenter) {
            flowBlock(model.trackState(trackId).tapOf { it?.capo }) { capo ->
                textBlock(capo?.let { "capo $it" } ?: "no capo", modify(TextSmall, InkDimFg))
            }
            menuActionsRow(listOf(MenuAction("New section", config = { setPopoverTarget(newId, true) }) { }))
        }
        // keyed by the sections and their names, so typing in one leaves every card in place
        flowBlock(model.trackSectionsState(trackId), FlexColumn) { sections ->
            sections.forEach { sectionCard(trackId, it, track.notationLabel) }
        }
        arrangementsCard(trackId)
        flowBlock(model.sectionWarningsState(trackId)) { warnings ->
            warnings.forEach { textBlock("Warning: $it", modify(TextSmall, InkDimFg)) }
        }
    }
}

/**
 * Renders [section] of the track [trackId] in a card under its name, with actions to edit its properties and to remove
 * it: its text, labeled [label], and its notation.
 */
context(studio: MusicStudio, model: MusicEditor)
private fun ViewScope.sectionCard(trackId: TrackId, section: MusicSection, label: String) {
    val editor = SectionEditor(contentScope, model, trackId, section.sectionId)
    val editId = Id(Uuid.random().toString())
    sectionEditPopover(editId, trackId, section)
    actionSectionCard(
        label = section.name,
        actions = listOf(
            MenuAction("Edit", config = { setPopoverTarget(editId, true) }) { },
            MenuAction("Remove") { model.removeSection(trackId, section.sectionId) },
        ),
    ) {
        notationPanes(label, editor.textState, editor.errorsState, studio.notationViewState) {
            flowBlock(editor.notationState) { notation -> notation?.let { sectionStrips(it) } }
        }
    }
}

/**
 * A popover with [id] holding the name of [section], and the stanzas sung to it when the track [trackId] holds it as a
 * phrase, which its save button applies. The name changes in every track.
 */
context(model: MusicEditor)
private fun ViewScope.sectionEditPopover(id: Id, trackId: TrackId, section: MusicSection) {
    val phrase = model.readTrack(trackId)?.sections?.firstOrNull { it.sectionId == section.sectionId } as? Phrase
    popover(id) { popover ->
        val nameState = storeOf(section.name)
        val lyricsState = storeOf(phrase?.lyrics)
        column(Padding(2)) {
            textField(nameState, "name")
            if (phrase != null) lyricPicks(lyricsState)
            button("Save", {
                if (phrase != null) model.setSectionLyrics(trackId, section.sectionId, lyricsState.now)
                model.renameSection(section.sectionId, nameState.now)
                popover.close()
            })
        }
    }
}

/**
 * Renders the choice between any stanza and named ones, and the stanzas named, bound to [lyricsState], which is `null`
 * for any. Naming them starts from none.
 */
context(model: MusicEditor)
private fun ViewScope.lyricPicks(lyricsState: MutableTap<List<SectionId>?>) {
    val choices = model.readLyricChoices()
    val isNamedState = lyricsState.mutableTapOf({ it != null }) { isNamed -> if (isNamed) this ?: emptyList() else null }
    switch("Name stanzas", isNamedState)
    flowBlock(lyricsState) { lyrics ->
        if (lyrics == null) return@flowBlock
        val picks = lyrics.map { sectionId -> choices.firstOrNull { it.sectionId == sectionId } ?: MusicSection(sectionId, UNNAMED_SECTION) }
        sectionPicks(picks, choices) { lyricsState.set(it) }
    }
}

/**
 * Renders the arrangements of the track [trackId] in a card, with an action that adds one: each under the name of the
 * section it stands for, with the sections it plays.
 */
context(model: MusicEditor)
private fun ViewScope.arrangementsCard(trackId: TrackId) {
    val newId = Id(Uuid.random().toString())
    namePopover(newId, "Add") { model.addArrangement(trackId, it) }
    actionSectionCard("Arrangements", listOf(MenuAction("New", config = { setPopoverTarget(newId, true) }) { })) {
        flowBlock(model.trackArrangementsState(trackId), FlexColumn) { board ->
            board.arrangements.forEach { arrangement ->
                row(AlignItemsCenter) {
                    textBlock(arrangement.section.name)
                    sectionPicks(arrangement.sections, board.choices.filter { it != arrangement.section }) {
                        model.setArrangement(trackId, arrangement.section.sectionId, it)
                    }
                    button("Remove", { model.removeSection(trackId, arrangement.section.sectionId) }, TextSmall)
                }
            }
            if (board.arrangements.isEmpty()) textBlock("No arrangements.", modify(TextSmall, InkDimFg))
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

/** The icon of the track's type. */
val Track.svg get() = when (this) {
    is ChordTrack -> SvgFile.AcousticGuitar
    is MelodyTrack -> SvgFile.Music
    is LyricTrack -> SvgFile.Microphone
    is DrumTrack -> SvgFile.Drum
}

/** The name of the notation the track's sections are written in. */
private val Track.notationLabel get() = when (this) {
    is ChordTrack -> "chords"
    is MelodyTrack -> "jianpu"
    is LyricTrack -> "lyrics"
    is DrumTrack -> "drums"
}
