package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.music.Arrangement
import streetlight.model.music.Beats
import streetlight.model.music.Groove
import streetlight.model.music.Music
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Section
import streetlight.model.music.SectionId
import streetlight.model.music.Stanza
import streetlight.model.music.Track
import streetlight.model.music.copyTrack
import streetlight.model.music.emptyNotationOf
import streetlight.model.music.lengthOf
import streetlight.model.music.nameOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.sectionIdsOf
import streetlight.model.music.withNotation
import streetlight.model.music.withoutNotation
import streetlight.model.music.ui.SectionCell
import streetlight.model.music.ui.cellsOf
import streetlight.model.studio.StudioDb

/**
 * The state behind the editor of [initialTrack], a track of [music], saved to [db] once edits pause and once more as
 * its scope ends. The music holds still while a track is in focus.
 */
class TrackEditor(
    scope: CoroutineScope,
    initialTrack: Track,
    private val music: Music,
    db: StudioDb,
) {
    private val state = storeOf(stateOf(initialTrack))

    val trackState = state.tapOf { it.track }
    val notationsState = state.tapOf { it.notations }
    val arrangementsState = state.tapOf { it.arrangements }
    val warningsState = state.tapOf { it.warnings }
    val sectionCellsState = state.tapOf { it.sectionCells }
    val cellsState = state.tapOf { it.cells }

    /** The track as it stands, with every edit made so far. */
    val trackNow get() = state.now.track

    init {
        scope.launch { trackState.saveChanges { db.track.saveTrack(it) } }
    }

    /**
     * Gives the track an empty notation named [name], which plays the section of its name when the track plays
     * nothing there. Returns whether it was added: not for a blank name, a name with a comma, or one the track holds.
     */
    fun addNotation(name: String) = addToTrack(name) { notationId, notationName ->
        withNotation(emptyNotationOf(notationId, notationName))
    }

    /** Gives the track an empty arrangement named [name], as [addNotation] does. */
    fun addArrangement(name: String) = addToTrack(name) { notationId, notationName ->
        copyTrack(arrangements = arrangements + Arrangement(notationId, notationName, emptyList()))
    }

    private fun addToTrack(name: String, add: Track.(NotationId, String) -> Track): Boolean {
        val notationName = name.trim().takeIf { it.isNotEmpty() && ',' !in it } ?: return false
        val track = state.now.track
        if (notationName in track.namesOf()) return false

        val notationId = NotationId.random()
        val sectionId = music.sectionOf(notationName)?.sectionId?.takeIf { it !in track.sections }
        updateTrack { held ->
            val added = held.add(notationId, notationName)
            if (sectionId == null) added else added.copyTrack(sections = added.sections + (sectionId to notationId))
        }
        return true
    }

    /** Removes the notation or arrangement [notationId], with the sections it played, and from the track's arrangements. */
    fun removeNotation(notationId: NotationId) = updateTrack { track ->
        val removed = track.withoutNotation(notationId)
        removed.copyTrack(arrangements = removed.arrangements.map { it.copy(notations = it.notations - notationId) })
    }

    /**
     * Names the notation or arrangement [notationId] [name]. A blank name, a name with a comma, or one another of the
     * track's holds leaves it as it is.
     */
    fun renameNotation(notationId: NotationId, name: String) = updateTrack { track ->
        val notationName = name.trim().takeIf { it.isNotEmpty() && ',' !in it } ?: return@updateTrack track
        val held = track.notations.filter { it.notationId != notationId }.mapNotNull { it.name } +
            track.arrangements.filter { it.notationId != notationId }.map { it.name }
        if (notationName in held) return@updateTrack track
        track.notations.firstOrNull { it.notationId == notationId }?.let { notation ->
            return@updateTrack track.withNotation(notation.withName(notationName))
        }
        track.copyTrack(arrangements = track.arrangements.map { if (it.notationId == notationId) it.copy(name = notationName) else it })
    }

    /** Sets the sections the notation or arrangement [notationId] plays, taking each from any other that played it. */
    fun setNotationSections(notationId: NotationId, sectionIds: List<SectionId>) = updateTrack { track ->
        val kept = track.sections.filter { (sectionId, held) -> held != notationId && sectionId !in sectionIds }
        track.copyTrack(sections = kept + sectionIds.associateWith { notationId })
    }

    /** Sets the notations the arrangement [notationId] plays, in order. */
    fun setArrangement(notationId: NotationId, notations: List<NotationId>) = updateTrack { track ->
        track.copyTrack(arrangements = track.arrangements.map {
            if (it.notationId == notationId) it.copy(notations = notations) else it
        })
    }

    private fun updateTrack(transform: (Track) -> Track) = state.update { stateOf(transform(it.track)) }

    // the notations, arrangements, warnings and cells follow the track, as the music holds still
    private fun stateOf(track: Track): TrackEditorState {
        fun namedOf(notationId: NotationId) = namedNotationOf(track, notationId, music)
        return TrackEditorState(
            track = track,
            notations = track.notations.map { namedOf(it.notationId) },
            arrangements = TrackArrangements(
                arrangements = track.arrangements.map { arrangement ->
                    NamedArrangement(namedOf(arrangement.notationId), arrangement.notations.map { namedOf(it) })
                },
                choices = (track.notations.map { it.notationId } + track.arrangements.map { it.notationId }).map { namedOf(it) },
            ),
            warnings = sectionWarningsOf(track, music),
            sectionCells = cellsOf(music),
            cells = cellsOf(music, track),
        )
    }
}

data class TrackEditorState(
    val track: Track,
    val notations: List<NamedNotation> = emptyList(),
    val arrangements: TrackArrangements = TrackArrangements(),
    val warnings: List<String> = emptyList(),
    val sectionCells: List<SectionCell> = emptyList(),
    val cells: List<SectionCell> = emptyList(),
)

/** The problems of [track] in [music]: each section it plays that its notation falls short of after its repeats, or runs past. */
fun sectionWarningsOf(track: Track, music: Music): List<String> = track.sections.mapNotNull { (sectionId, notationId) ->
    val length = track.lengthOf(notationId) ?: return@mapNotNull null
    val span = music.lengthOf(sectionId) ?: return@mapNotNull null
    val notationName = track.nameOf(notationId, music)
    val shortfall = span.ticks - length.ticks * track.repeatsOf(notationId, span)
    when {
        length > span -> "$notationName runs ${(length.ticks - span.ticks).toBeatsText()} beats past ${music.nameOf(sectionId)}"
        shortfall > 0 -> "$notationName falls ${shortfall.toBeatsText()} beats short of ${music.nameOf(sectionId)} after its repeats"
        else -> null
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
    name = track.nameOf(notationId, music),
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
