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
import koala.model.HeadingLevel
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
import streetlight.model.music.TrackType
import streetlight.model.music.text.NotationError
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.layout.menuActionsRow
import streetlight.web.dom.layout.iconActionsRow
import koala.model.IconAction
import streetlight.web.dom.layout.actionSection
import kampfire.model.MutableTap
import kampfire.model.Tap
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicFormProblem
import streetlight.web.model.NamedNotation
import streetlight.web.model.NotationEditor
import streetlight.web.model.WaveShelf
import streetlight.model.studio.StudioDb
import streetlight.web.model.readCapoText
import kotlinx.coroutines.launch
import streetlight.web.dom.layout.actionCard
import streetlight.web.model.MusicConfig
import streetlight.web.model.NotationView
import streetlight.web.model.TrackPlayer
import streetlight.web.model.toaster
import kotlin.uuid.Uuid

/** Renders the tracks of the song under an action that adds one, each a row that brings its editor into focus. */
context(config: MusicConfig, model: MusicEditor, db: StudioDb, waveShelf: WaveShelf, player: TrackPlayer)
fun ViewScope.trackEditor() {
    val newId = Id(Uuid.random().toString())
    popover(newId) { popover ->
        actionMenu(TrackType.entries.map { trackType ->
            MenuAction(trackType.name) {
                model.addTrack(trackType)
                popover.close()
            }
        })
    }
    actionSection(
        label = "Tracks",
        level = HeadingLevel.H4,
        icons = listOf(IconAction(SvgFile.Plus.small, "New", config = { setPopoverTarget(newId, true) }) { }),
    ) {
        // keyed by id, so an edit to a track's content or name or a move leaves its row in place
        orderColumn(model.tracksState, { it.trackId }, Transitioner) { track -> trackRow(track) }
        flowBlock(model.tracksState.tapOf { it.isEmpty() }) { isEmpty ->
            if (isEmpty) textBlock("No tracks yet.", modify(TextAlignCenter, Italic, TextSmall, OpacityHigh))
        }
    }
}

/**
 * Renders the row of [track], which brings its editor into focus, with a grip that moves it among the tracks.
 */
context(handle: OrderHandle, config: MusicConfig, model: MusicEditor, waveShelf: WaveShelf, player: TrackPlayer)
private fun ViewScope.trackRow(track: Track) {
    val trackId = track.trackId
    val nameId = Id(Uuid.random().toString())
    val key = trackId.toString()
    trackEditPopover(nameId, track)
    // named apart from its item, so it pops in with the tab as its item would, and pairs with its focus card
    row(modify(AlignItemsCenter, track.trackType.color.bg, Padding(1), transition("card-$key"), Presto.Pop)) {
        handle.grip(icon(SvgFile.GripVertical.small, modify(IconStyle.DefaultMod, Attribute.AriaLabel.to("Move"))))
        button({ model.focusTrack(trackId) }, Flex1) {
            row(AlignItemsCenter) {
                icon(track.trackType.svg.large, Height(5))
                flowBlock(model.trackState(trackId).tapOf { it?.name.orEmpty() }) { name ->
                    heading5(name)
                }
            }
        }
        // a lyric track makes no sound, so it has nothing to play
        val play = MenuAction("Play") {
            if (player.isPlayingState.now) player.stop()
            else model.readTrack(trackId)?.let { track ->
                contentScope.launch { player.play(model.sheetState.now, listOf(track), musicWaves = waveShelf.musicWavesOf(model.sheetState.now)) }
            }
        }.takeIf { track !is LyricTrack }
        menuActionsRow(listOfNotNull(
            play,
            MenuAction("Remove") { model.removeTrack(trackId) },
        ))
        val edit = IconAction(SvgFile.Gear.small, "Edit", config = { setPopoverTarget(nameId, true) }) { }
        iconActionsRow(listOf(edit), SmallIconHeight)
    }
}

/**
 * Renders [track] alone in a card under its name and its type's icon, with an up arrow that returns to the overview: its
 * notations and arrangements, with actions to record a take of the whole track and bind a clip to it.
 */
context(config: MusicConfig, model: MusicEditor, db: StudioDb)
fun ViewScope.trackFocus(track: Track) {
    actionCard(
        label = track.name,
        actions = clipActions(track.trackId, null),
        level = HeadingLevel.H3,
        mod = modify(track.trackType.color.bg, OutlineLow),
        svg = track.trackType.svg.large,
        key = track.trackId.toString(),
        onBack = model::clearFocus,
    ) {
        card(modify(PaperBg, Outline, BorderRadius1)) {
            trackContent(track.trackId)
        }
    }
}

/**
 * A popover with [id] holding the name and capo of [track], and the lyric track a melody is sung with, which its save
 * button applies.
 */
context(model: MusicEditor)
private fun ViewScope.trackEditPopover(id: Id, track: Track) {
    popover(id) { popover ->
        val nameState = storeOf(track.name)
        val capoTextState = storeOf(track.capo?.toString().orEmpty())
        val lyricTracks = model.lyricTracksState.now
        val lyricNames = listOf(FIRST_LYRICS) + lyricTracks.map { it.name }
        val lyricsState = storeOf((track as? MelodyTrack)?.lyrics?.let { id -> lyricTracks.firstOrNull { it.trackId == id }?.name } ?: FIRST_LYRICS)
        val save = save@{
            val capo = readCapoText(capoTextState.now) ?: return@save toaster.deliver(MusicFormProblem.capo)
            model.setTrackCapo(track.trackId, capo.value)
            model.renameTrack(track.trackId, nameState.now)
            if (track is MelodyTrack) model.setTrackLyrics(track.trackId, lyricTracks.firstOrNull { it.name == lyricsState.now }?.trackId)
            popover.close()
        }
        column(Padding(2)) {
            row(AlignItemsCenter) {
                textField(nameState, "name", Flex1, onEnterSubmit = save)
                textField(capoTextState, "capo", size = 4)
            }
            if (track is MelodyTrack) dropMenu(lyricNames, lyricsState)
            button("Save", save)
        }
    }
}

/**
 * Renders the content of the track [trackId]: its properties, with an action that adds a notation, a card for each of
 * its notations, and its arrangements.
 */
context(config: MusicConfig, model: MusicEditor, db: StudioDb)
private fun ViewScope.trackContent(trackId: TrackId) {
    val track = model.readTrack(trackId) ?: return
    val newId = Id(Uuid.random().toString())
    namePopover(newId, "Add") { model.addNotation(trackId, it) }
    column(PaddingY(1)) {
        row(AlignItemsCenter) {
            flowBlock(model.trackState(trackId).tapOf { it?.capo }) { capo ->
                textBlock(capo?.let { "capo $it" } ?: "no capo", modify(TextSmall, InkDimFg))
            }
            menuActionsRow(listOf(MenuAction("New notation", config = { setPopoverTarget(newId, true) }) { }))
        }
        // keyed by the notations, their names and sections, so typing in one leaves every card in place
        flowBlock(model.trackNotationsState(trackId), FlexColumn) { notations ->
            notations.forEach { notationCard(trackId, it, track.notationLabel) }
        }
        arrangementsCard(trackId)
        flowBlock(model.sectionWarningsState(trackId)) { warnings ->
            warnings.forEach { textBlock("Warning: $it", modify(TextSmall, InkDimFg)) }
        }
    }
}

/**
 * Renders [notation] of the track [trackId] in a card under its name, with actions to edit it, to record a take of it,
 * to bind a clip to it and to remove it: the sections it plays, then its text, labeled [label], and its notes.
 */
context(config: MusicConfig, model: MusicEditor, db: StudioDb)
private fun ViewScope.notationCard(trackId: TrackId, notation: NamedNotation, label: String) {
    val notationState = model.notationState(trackId, notation.notationId)
    val editor = NotationEditor(contentScope, config.notationStyleState, model.sheetState, notationState, trackId, notation.notationId)
    val editId = Id(Uuid.random().toString())
    namePopover(editId, "Save") { name ->
        model.renameNotation(trackId, notation.notationId, name)
        true
    }
    actionCard(
        label = notation.name,
        actions = listOf(MenuAction("Rename", config = { setPopoverTarget(editId, true) }) { }) +
            clipActions(trackId, notation.notationId) +
            MenuAction("Remove") { model.removeNotation(trackId, notation.notationId) },
    ) {
        notationSectionPicks(trackId, notation)
        notationPanes(label, editor.textState, editor.errorsState, config.notationViewState) {
            flowBlock(editor.sectionNotationState) { sectionNotation -> sectionNotation?.let { sectionStrips(it) } }
        }
    }
}

/** Renders the sections [notation] of the track [trackId] plays, with a menu of the music's sections to add one. */
context(model: MusicEditor)
private fun ViewScope.notationSectionPicks(trackId: TrackId, notation: NamedNotation) {
    row(AlignItemsCenter) {
        textBlock("plays", modify(TextSmall, InkDimFg))
        namedPicks(notation.sections, model.sectionsState.now.filter { it !in notation.sections }, { it.name }) { sections ->
            model.setNotationSections(trackId, notation.notationId, sections.map { it.sectionId })
        }
    }
}

/**
 * Renders the arrangements of the track [trackId] in a card, with an action that adds one: each under its name, with
 * the notations it plays and the sections it plays them for.
 */
context(model: MusicEditor)
private fun ViewScope.arrangementsCard(trackId: TrackId) {
    val newId = Id(Uuid.random().toString())
    namePopover(newId, "Add") { model.addArrangement(trackId, it) }
    actionCard("Arrangements", listOf(MenuAction("New", config = { setPopoverTarget(newId, true) }) { })) {
        flowBlock(model.trackArrangementsState(trackId), FlexColumn) { board ->
            board.arrangements.forEach { (arrangement, notations) ->
                column {
                    row(AlignItemsCenter) {
                        textBlock(arrangement.name)
                        val choices = board.choices.filter { it.notationId != arrangement.notationId }
                        namedPicks(notations, choices, { it.name }) { picks ->
                            model.setArrangement(trackId, arrangement.notationId, picks.map { it.notationId })
                        }
                        button("Remove", { model.removeNotation(trackId, arrangement.notationId) }, TextSmall)
                    }
                    notationSectionPicks(trackId, arrangement)
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

/** The name of the lyric choice that sings a melody with the music's first lyric track. */
private const val FIRST_LYRICS = "First lyrics"

/** The name of the text the track's notations are written in. */
private val Track.notationLabel get() = when (this) {
    is ChordTrack -> "chords"
    is MelodyTrack -> "jianpu"
    is LyricTrack -> "lyrics"
    is DrumTrack -> "drums"
}
