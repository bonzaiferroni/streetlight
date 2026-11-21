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
import streetlight.model.music.Notation
import streetlight.model.music.Track
import streetlight.model.music.TrackType
import streetlight.model.music.sectionIdsOf
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
import streetlight.web.model.MusicHost
import streetlight.web.model.NamedNotation
import streetlight.web.model.NotationFocus
import streetlight.web.model.TrackEditor
import streetlight.web.model.TrackFocus
import streetlight.web.model.TrackPlayer
import streetlight.web.model.WaveShelf
import streetlight.web.model.readCapoText
import streetlight.web.model.toaster
import kotlin.uuid.Uuid

context(config: MusicConfig, host: MusicHost, db: StudioDb)
fun ViewScope.trackEditor(focus: TrackFocus, setFocus: (MusicEditorFocus?) -> Unit) {
    val model = TrackEditor(contentScope, focus.trackId, host)
    val track = host.readTrack(focus.trackId)
    actionCard(
        label = track.name,
        level = HeadingLevel.H3,
        actions = clipActions(focus.trackId, null),
        mod = modify(track.trackType.color.bg, OutlineLow),
        svg = track.trackType.svg.large,
        key = focus.trackId.toString(),
        onBack = { setFocus(null) },
    ) {
        card(modify(PaperBg, Outline, BorderRadius1)) {
            context(model) {
                trackTimeline(setFocus)
                trackContent(setFocus)
            }
        }
    }
}

/**
 * Renders the track of [model] as its row of the overview, under the row of the song's sections, where a pass hands
 * its notation to [setFocus].
 */
context(model: TrackEditor)
private fun ViewScope.trackTimeline(setFocus: (MusicEditorFocus?) -> Unit) {
    flowBlock(model.cellsState, OverviewStyle.Grid) { cells ->
        val track = model.trackState.now
        sectionRow(model.sectionCellsState.now, null) { }
        sectionRow(cells, track.trackType, { pass -> setFocus(NotationFocus(track.trackId, pass.notationId)) }) {
            trackLabel(track)
        }
    }
}

/**
 * Renders the content of the track of [model]: its capo, with an action that adds a notation, a row for each of its
 * notations, and its arrangements, then its warnings.
 */
context(model: TrackEditor, host: MusicHost, db: StudioDb)
private fun ViewScope.trackContent(setFocus: (MusicEditorFocus?) -> Unit) {
    val newId = Id(Uuid.random().toString())
    // namePopover(newId, "Add") { model.addNotation(it) }
    column(PaddingY(1)) {
        row(AlignItemsCenter) {
            flowBlock(model.trackState.tapOf { it.capo }) { capo ->
                textBlock(capo?.let { "capo $it" } ?: "no capo", modify(TextSmall, InkDimFg))
            }
            menuActionsRow(listOf(MenuAction("New notation", config = { setPopoverTarget(newId, true) }) { }))
        }
        flowBlock(model.notationsState, FlexColumn) { notations ->
            notations.forEach { notationRow(it, setFocus) }
            if (notations.isEmpty()) textBlock("No notations yet.", modify(TextSmall, InkDimFg))
        }
        arrangementsCard()
        flowBlock(model.warningsState) { warnings ->
            warnings.forEach { textBlock("Warning: $it", modify(TextSmall, InkDimFg)) }
        }
    }
}

/**
 * Renders [notation] as a row under its name, which [setFocus] brings into focus to edit its notes, with the sections
 * it plays and actions to rename it, record a clip of it, bind a clip to it and remove it.
 */
context(editor: TrackEditor, host: MusicHost, db: StudioDb)
private fun ViewScope.notationRow(notation: Notation, setFocus: (MusicEditorFocus?) -> Unit) {
    val renameId = Id(Uuid.random().toString())
//    namePopover(renameId, "Save") { name ->
//        editor.renameNotation(notation.notationId, name)
//        true
//    }
    actionCard(
        label = notation.label,
//        actions = buildList {
//            addAll(
//                listOf(
//                    MenuAction("Edit") { setFocus(NotationFocus(trackId, notation.notationId)) },
//                    MenuAction("Rename", config = { setPopoverTarget(renameId, true) }) { },
//                )
//            )
//            addAll(this@notationRow.clipActions(trackId, notation.notationId))
//            add(MenuAction("Remove") { editor.removeNotation(notation.notationId) })
//        },
    ) {
        notationSectionPicks(notation)
    }
}

/** Renders the sections [notation] plays, with a menu of the music's sections to add one. */
context(model: TrackEditor, host: MusicHost)
private fun ViewScope.notationSectionPicks(notation: Notation) {
    val sections = host.musicState.now.sections
    val picks = model.trackState.now.sectionIdsOf(notation.notationId)
        .mapNotNull { id -> sections.firstOrNull { it.sectionId == id } }

    row(AlignItemsCenter) {
        textBlock("plays", modify(TextSmall, InkDimFg))
        namedPicks(picks, sections, { it.name }) { sections ->
            // model.setNotationSections(notation.notationId, sections.map { it.sectionId })
        }
    }
}

/**
 * Renders the arrangements of the track of [model] in a card, with an action that adds one: each under its name,
 * with the notations it plays and the sections it plays them for.
 */
context(model: TrackEditor, host: MusicHost)
private fun ViewScope.arrangementsCard() {
    val newId = Id(Uuid.random().toString())
    // namePopover(newId, "Add") { model.addArrangement(it) }
    actionCard("Arrangements", listOf(MenuAction("New", config = { setPopoverTarget(newId, true) }) { })) {
        flowBlock(model.arrangementsState, FlexColumn) { board ->
//            board.arrangements.forEach { (arrangement, notations) ->
//                column {
//                    row(AlignItemsCenter) {
//                        textBlock(arrangement.name)
//                        val choices = board.choices.filter { it.notationId != arrangement.notationId }
//                        namedPicks(notations, choices, { it.name }) { picks ->
//                            model.setArrangement(arrangement.notationId, picks.map { it.notationId })
//                        }
//                        button("Remove", { model.removeNotation(arrangement.notationId) }, TextSmall)
//                    }
//                    notationSectionPicks(arrangement)
//                }
//            }
//            if (board.arrangements.isEmpty()) textBlock("No arrangements.", modify(TextSmall, InkDimFg))
        }
    }
}
