package streetlight.web.model

import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.music.Beats
import streetlight.model.music.Groove
import streetlight.model.music.Music
import streetlight.model.music.MusicSheet
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Section
import streetlight.model.music.Stanza
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.lengthOf
import streetlight.model.music.nameOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.sectionIdsOf
import streetlight.model.music.trackOrNullOf
import streetlight.model.music.ui.SectionCell
import streetlight.model.music.ui.getSectionCells
import streetlight.model.music.ui.notationCellsOf

/**
 * The state behind the editor of [initialTrack], a track of [music], saved to [db] once edits pause and once more as
 * its scope ends. The music holds still while a track is in focus.
 */
class TrackEditor(
    scope: CoroutineScope,
    private val trackId: TrackId,
    private val host: MusicHost,
) {
    val sheetState = host.sheetState
    val trackState = host.trackStateOf(trackId)
    val notationsState = trackState.tapOf { it.notations }
    val arrangementsState = trackState.tapOf { it.arrangements }
    val warningsState = sheetState.tapOf { it.sectionWarningsOf(trackId) }
    val sectionCellsState = sheetState.tapOf { it.getSectionCells() }
    val cellsState = sheetState.tapOf { it.notationCellsOf(trackId) }
}

data class TrackEditorState(
    val warnings: List<String> = emptyList(),
    val sectionCells: List<SectionCell> = emptyList(),
    val cells: List<SectionCell> = emptyList(),
)

/** The problems of [track] in [music]: each section it plays that its notation falls short of after its repeats, or runs past. */
fun MusicSheet.sectionWarningsOf(trackId: TrackId): List<String> {
    val track = trackOrNullOf(trackId) ?: return emptyList()
    return track.sections.mapNotNull { (sectionId, notationId) ->
        val length = track.lengthOf(notationId) ?: return@mapNotNull null
        val span = music.lengthOf(sectionId) ?: return@mapNotNull null
        val notationName = track.nameOf(notationId)
        val shortfall = span.ticks - length.ticks * track.repeatsOf(notationId, span)
        when {
            length > span -> "$notationName runs ${(length.ticks - span.ticks).toBeatsText()} beats past ${music.nameOf(sectionId)}"
            shortfall > 0 -> "$notationName falls ${shortfall.toBeatsText()} beats short of ${music.nameOf(sectionId)} after its repeats"
            else -> null
        }
    }
}

/** The notation or arrangement [notationId] of a track as it is shown, under its [name], with the [sections] it plays. */
data class NamedNotation(
    val notationId: NotationId,
    val name: String,
    val sections: List<Section>,
)

/** The arrangement [arrangement] as it is shown: the [notations] it plays in order. */
data class NamedArrangement(
    val arrangement: NamedNotation,
    val notations: List<NamedNotation>,
)

/** The [arrangements] of a track, and the [choices] of its notations an arrangement may play. */
data class TrackArrangements(
    val arrangements: List<NamedArrangement> = emptyList(),
    val choices: List<NamedNotation> = emptyList(),
)

private fun namedNotationOf(track: Track, notationId: NotationId, music: Music) = NamedNotation(
    notationId = notationId,
    name = track.nameOf(notationId),
    sections = track.sectionIdsOf(notationId).mapNotNull { sectionId -> music.sections.firstOrNull { it.sectionId == sectionId } },
)

/** The names of this track's notations and arrangements. */
private fun Track.namesOf() = notations.mapNotNull { it.name } + arrangements.map { it.name }

/** This notation named [name]. */
private fun Notation.withName(name: String): Notation = when (this) {
    is Progression -> copy(name = name)
    is Phrase -> copy(name = name)
    is Stanza -> copy(name = name)
    is Groove -> copy(name = name)
}

private fun Int.toBeatsText() = (toDouble() / Beats.TICKS_PER_BEAT).let {
    if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
}
