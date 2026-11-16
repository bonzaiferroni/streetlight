package streetlight.web.view

import kampfire.model.storeOf
import kampfire.model.tapOf
import koala.SvgFile
import koala.component.IconStyle
import koala.component.heading5
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.OrderHandle
import koala.dom.layout.flowBlock
import koala.dom.layout.orderColumn
import koala.model.HeadingLevel
import koala.model.IconAction
import koala.model.MenuAction
import koala.modifier.*
import koala.page.Id
import kotlinx.coroutines.launch
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Track
import streetlight.model.music.TrackType
import streetlight.model.studio.StudioDb
import streetlight.web.dom.layout.actionCard
import streetlight.web.dom.layout.actionMenu
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.iconActionsRow
import streetlight.web.dom.layout.menuActionsRow
import streetlight.web.model.MusicConfig
import streetlight.web.model.MusicEditor
import streetlight.web.model.MusicEditorFocus
import streetlight.web.model.MusicFormProblem
import streetlight.web.model.NamedNotation
import streetlight.web.model.NotationFocus
import streetlight.web.model.TrackEditor
import streetlight.web.model.TrackFocus
import streetlight.web.model.TrackPlayer
import streetlight.web.model.WaveShelf
import streetlight.web.model.readCapoText
import streetlight.web.model.toaster
import kotlin.uuid.Uuid

/** Renders the tracks of the song under an action that adds one, each a row that brings its editor into focus. */
context(config: MusicConfig, model: MusicEditor, waveShelf: WaveShelf, player: TrackPlayer)
fun ViewScope.trackList() {
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
        button({ model.readTrack(trackId)?.let { model.setFocus(TrackFocus(it)) } }, Flex1) {
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
            val lyrics = lyricTracks.firstOrNull { it.name == lyricsState.now }?.trackId
            model.editTrack(track.trackId, nameState.now, capo.value, lyrics)
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
 * Renders [initialTrack] alone in a card under its name and its type's icon, with actions to record a clip of the
 * whole track and bind one to it, and an up arrow that hands [setFocus] the overview: its properties, a row for each
 * of its notations, which [setFocus] brings into focus, and its arrangements.
 */
context(config: MusicConfig, model: MusicEditor, db: StudioDb)
fun ViewScope.trackEditor(initialTrack: Track, setFocus: (MusicEditorFocus?) -> Unit) {
    val editor = TrackEditor(contentScope, initialTrack, model.musicState.now, db)
    val trackId = initialTrack.trackId
    actionCard(
        label = initialTrack.name,
        level = HeadingLevel.H3,
        actions = clipActions(trackId, null),
        mod = modify(initialTrack.trackType.color.bg, OutlineLow),
        svg = initialTrack.trackType.svg.large,
        key = trackId.toString(),
        onBack = { setFocus(null) },
    ) {
        card(modify(PaperBg, Outline, BorderRadius1)) {
            context(editor) { trackContent(setFocus) }
        }
    }
}

/**
 * Renders the content of the track of [editor]: its capo, with an action that adds a notation, a row for each of its
 * notations, and its arrangements, then its warnings.
 */
context(model: MusicEditor, editor: TrackEditor, db: StudioDb)
private fun ViewScope.trackContent(setFocus: (MusicEditorFocus?) -> Unit) {
    val newId = Id(Uuid.random().toString())
    namePopover(newId, "Add") { editor.addNotation(it) }
    column(PaddingY(1)) {
        row(AlignItemsCenter) {
            flowBlock(editor.trackState.tapOf { it.capo }) { capo ->
                textBlock(capo?.let { "capo $it" } ?: "no capo", modify(TextSmall, InkDimFg))
            }
            menuActionsRow(listOf(MenuAction("New notation", config = { setPopoverTarget(newId, true) }) { }))
        }
        flowBlock(editor.notationsState, FlexColumn) { notations ->
            notations.forEach { notationRow(it, setFocus) }
            if (notations.isEmpty()) textBlock("No notations yet.", modify(TextSmall, InkDimFg))
        }
        arrangementsCard()
        flowBlock(editor.warningsState) { warnings ->
            warnings.forEach { textBlock("Warning: $it", modify(TextSmall, InkDimFg)) }
        }
    }
}

/**
 * Renders [notation] as a row under its name, which [setFocus] brings into focus to edit its notes, with the sections
 * it plays and actions to rename it, record a clip of it, bind a clip to it and remove it.
 */
context(model: MusicEditor, editor: TrackEditor, db: StudioDb)
private fun ViewScope.notationRow(notation: NamedNotation, setFocus: (MusicEditorFocus?) -> Unit) {
    val trackId = editor.trackNow.trackId
    val renameId = Id(Uuid.random().toString())
    namePopover(renameId, "Save") { name ->
        editor.renameNotation(notation.notationId, name)
        true
    }
    actionCard(
        label = notation.name,
        actions = listOf(
            MenuAction("Edit") { setFocus(NotationFocus(editor.trackNow, notation.notationId)) },
            MenuAction("Rename", config = { setPopoverTarget(renameId, true) }) { },
        ) + clipActions(trackId, notation.notationId) + MenuAction("Remove") { editor.removeNotation(notation.notationId) },
    ) {
        notationSectionPicks(notation)
    }
}

/** Renders the sections [notation] plays, with a menu of the music's sections to add one. */
context(model: MusicEditor, editor: TrackEditor)
private fun ViewScope.notationSectionPicks(notation: NamedNotation) {
    row(AlignItemsCenter) {
        textBlock("plays", modify(TextSmall, InkDimFg))
        namedPicks(notation.sections, model.sectionsState.now.filter { it !in notation.sections }, { it.name }) { sections ->
            editor.setNotationSections(notation.notationId, sections.map { it.sectionId })
        }
    }
}

/**
 * Renders the arrangements of the track of [editor] in a card, with an action that adds one: each under its name,
 * with the notations it plays and the sections it plays them for.
 */
context(model: MusicEditor, editor: TrackEditor)
private fun ViewScope.arrangementsCard() {
    val newId = Id(Uuid.random().toString())
    namePopover(newId, "Add") { editor.addArrangement(it) }
    actionCard("Arrangements", listOf(MenuAction("New", config = { setPopoverTarget(newId, true) }) { })) {
        flowBlock(editor.arrangementsState, FlexColumn) { board ->
            board.arrangements.forEach { (arrangement, notations) ->
                column {
                    row(AlignItemsCenter) {
                        textBlock(arrangement.name)
                        val choices = board.choices.filter { it.notationId != arrangement.notationId }
                        namedPicks(notations, choices, { it.name }) { picks ->
                            editor.setArrangement(arrangement.notationId, picks.map { it.notationId })
                        }
                        button("Remove", { editor.removeNotation(arrangement.notationId) }, TextSmall)
                    }
                    notationSectionPicks(arrangement)
                }
            }
            if (board.arrangements.isEmpty()) textBlock("No arrangements.", modify(TextSmall, InkDimFg))
        }
    }
}

/** The name of the lyric choice that sings a melody with the music's first lyric track. */
private const val FIRST_LYRICS = "First lyrics"
