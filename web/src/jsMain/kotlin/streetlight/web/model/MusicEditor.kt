@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import streetlight.model.music.Arrangement
import streetlight.model.music.Groove
import streetlight.model.music.Music
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Stanza
import streetlight.model.music.Beats
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.MusicSheet
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.Section
import streetlight.model.music.SectionId
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.TrackType
import streetlight.model.music.copyTrack
import streetlight.model.music.emptyNotationOf
import streetlight.model.music.emptyTrackOf
import streetlight.model.music.freeTrackNameOf
import streetlight.model.music.lengthOf
import streetlight.model.music.nameOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.sectionIdsOf
import streetlight.model.music.withNotation
import streetlight.model.music.withoutNotation
import streetlight.model.studio.StudioDb

/** The state behind the editor of the music and tracks of [initialSheet], saved to [db] once edits pause. */
class MusicEditor(
    scope: CoroutineScope,
    initialSheet: MusicSheet,
    private val db: StudioDb,
) {
    private val state = storeOf(MusicEditorState(initialSheet.music, initialSheet.tracks))
    val musicState = state.mutableTapOf({ it.music }) { copy(music = it) }
    val tracksState = state.mutableTapOf({ it.tracks }) { copy(tracks = it) }

    /** The music with its tracks, following each change to either. */
    val sheetState: Tap<MusicSheet> = state.tapOf { MusicSheet(it.music, it.tracks) }

    init {
        scope.launch { saveSheetChanges() }
    }

    /**
     * The warnings on the track [trackId]: each section it plays that its notation falls short of after its repeats,
     * or runs past.
     */
    fun sectionWarningsState(trackId: TrackId) = state.tapOf { editor ->
        val music = editor.music
        val track = editor.tracks.firstOrNull { it.trackId == trackId } ?: return@tapOf emptyList()
        track.sections.mapNotNull { (sectionId, notationId) ->
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
    }

    fun readTrack(index: Int) = state.now.tracks.getOrNull(index)

    fun readTrack(trackId: TrackId) = state.now.tracks.firstOrNull { it.trackId == trackId }

    /** The track at [index]. */
    fun trackState(index: Int) = state.mutableTapOf({ it.tracks.getOrNull(index) }) { track ->
        if (track == null) this
        else copy(tracks = tracks.toMutableList().apply { set(index, track) })
    }

    /** The track [trackId]. */
    fun trackState(trackId: TrackId) = state.mutableTapOf({ editor -> editor.tracks.firstOrNull { it.trackId == trackId } }) { track ->
        if (track == null) this
        else copy(tracks = tracks.map { if (it.trackId == trackId) track else it })
    }

    /** The notation [notationId] of the track [trackId], or `null` while the track holds no such notation. */
    fun notationState(trackId: TrackId, notationId: NotationId): MutableTap<Notation?> =
        trackState(trackId).mutableTapOf({ track -> track?.notations?.firstOrNull { it.notationId == notationId } }) { notation ->
            if (notation == null) this else this?.withNotation(notation)
        }

    /**
     * The track [trackId] as a [T], reading [empty] while there is no such track.
     *
     * A removal can reach the lens before the view reading it is rebuilt, which then reads [empty].
     */
    inline fun <reified T : Track> trackState(trackId: TrackId, empty: T): MutableTap<T> =
        trackState(trackId).mutableTapOf({ it as? T ?: empty }) { it }

    /** The track whose editor is shown, in place of the overview, or `null`. */
    val focusState: Tap<TrackId?> = state.tapOf { it.focus }

    /** Shows the editor of the track [trackId] alone. */
    fun focusTrack(trackId: TrackId) = state.update { it.copy(focus = trackId) }

    /** Returns from the track in focus to the overview. */
    fun clearFocus() = state.update { it.copy(focus = null) }

    /** Adds an empty track of [trackType], named for its type under a name no track holds, and brings it into focus. */
    fun addTrack(trackType: TrackType) = state.update { editor ->
        val track = emptyTrackOf(trackType, editor.music.musicId, editor.tracks.newTrackName(trackType.name))
        editor.copy(tracks = editor.tracks + track, focus = track.trackId)
    }

    /**
     * Names the track [trackId] [name], numbered when another track holds it. A blank name leaves the track as it
     * is.
     */
    fun renameTrack(trackId: TrackId, name: String) = state.update { editor ->
        val track = editor.tracks.firstOrNull { it.trackId == trackId } ?: return@update editor
        val base = name.trim().takeIf { it.isNotEmpty() && it != track.name } ?: return@update editor
        val freeName = editor.tracks.filter { it.trackId != trackId }.newTrackName(base)
        editor.copy(tracks = editor.tracks.map { if (it.trackId == trackId) track.copyTrack(name = freeName) else it })
    }

    /** Removes the track [trackId], and with the save that follows, its bound clips and its studies. */
    fun removeTrack(trackId: TrackId) = state.update { editor ->
        editor.copy(
            tracks = editor.tracks.filter { it.trackId != trackId },
            focus = editor.focus?.takeIf { it != trackId },
        )
    }

    /** Sets the capo of the track [trackId], `null` for none. */
    fun setTrackCapo(trackId: TrackId, capo: Int?) = updateTrack(trackId) { it.copyTrack(capo = capo) }

    /** The notations of the track [trackId], in its order, each with its name and the sections it plays. */
    fun trackNotationsState(trackId: TrackId): Tap<List<NamedNotation>> = state.tapOf { editor ->
        val track = editor.tracks.firstOrNull { it.trackId == trackId } ?: return@tapOf emptyList()
        track.notations.map { notation ->
            NamedNotation(notation.notationId, track.nameOf(notation.notationId, editor.music), editor.music.sectionsOf(track, notation.notationId))
        }
    }

    /** The arrangements of the track [trackId], and the notations of the track they may play. */
    fun trackArrangementsState(trackId: TrackId): Tap<TrackArrangements> = state.tapOf { editor ->
        val track = editor.tracks.firstOrNull { it.trackId == trackId } ?: return@tapOf TrackArrangements()
        val music = editor.music
        fun namedOf(notationId: NotationId) = NamedNotation(notationId, track.nameOf(notationId, music), music.sectionsOf(track, notationId))
        TrackArrangements(
            arrangements = track.arrangements.map { arrangement ->
                NamedArrangement(namedOf(arrangement.notationId), arrangement.notations.map { namedOf(it) })
            },
            choices = (track.notations.map { it.notationId } + track.arrangements.map { it.notationId }).map { namedOf(it) },
        )
    }

    /** The arrangement of the music, each section it plays in order. */
    val arrangementState: Tap<List<Section>> = state.tapOf { editor ->
        editor.music.arrangement.mapNotNull { sectionId -> editor.music.sections.firstOrNull { it.sectionId == sectionId } }
    }

    /** The sections of the music. */
    val sectionsState: Tap<List<Section>> = state.tapOf { it.music.sections }

    /** The lyric tracks of the music, which a melody may be sung with. */
    val lyricTracksState: Tap<List<LyricTrack>> = state.tapOf { editor -> editor.tracks.filterIsInstance<LyricTrack>() }

    /**
     * Adds the section [name], [measures] long, to the end of the music's arrangement, joining the music's sections
     * when none holds the name. Returns whether it was added: not for a blank name, a name with a comma, or a
     * measure count below one.
     */
    fun addSection(name: String, measures: Int): Boolean {
        val sectionName = name.trim().takeIf { it.isNotEmpty() && ',' !in it } ?: return false
        if (measures < 1) return false
        state.update { editor ->
            val music = editor.music
            val section = music.sectionOf(sectionName) ?: Section(SectionId.random(), sectionName, measures)
            val sections = if (section in music.sections) music.sections else music.sections + section
            editor.copy(music = music.copy(sections = sections, arrangement = music.arrangement + section.sectionId))
        }
        return true
    }

    /**
     * Names the section [sectionId] [name], numbered when another section holds the name, and makes it [measures]
     * long. A blank name keeps the one it has, and a count below one keeps its measures.
     */
    fun editSection(sectionId: SectionId, name: String, measures: Int) = state.update { editor ->
        val held = editor.music.sections.filter { it.sectionId != sectionId }.mapTo(mutableSetOf()) { it.name }
        val base = name.trim().takeIf { it.isNotEmpty() && ',' !in it }
        val sections = editor.music.sections.map { section ->
            if (section.sectionId != sectionId) return@map section
            val freeName = base?.let { generateSequence(1) { it + 1 }.map { n -> if (n == 1) base else "$base $n" }.first { it !in held } }
            section.copy(name = freeName ?: section.name, measures = measures.takeIf { it >= 1 } ?: section.measures)
        }
        editor.copy(music = editor.music.copy(sections = sections))
    }

    /**
     * Gives the track [trackId] an empty notation named [name], which plays the section of its name when the track
     * plays nothing there. Returns whether it was added: not for a blank name, a name with a comma, or one the track
     * holds.
     */
    fun addNotation(trackId: TrackId, name: String) =
        addToTrack(trackId, name) { notationId, notationName -> withNotation(emptyNotationOf(notationId, notationName)) }

    /** Gives the track [trackId] an empty arrangement named [name], as [addNotation] does. */
    fun addArrangement(trackId: TrackId, name: String) = addToTrack(trackId, name) { notationId, notationName ->
        copyTrack(arrangements = arrangements + Arrangement(notationId, notationName, emptyList()))
    }

    private fun addToTrack(trackId: TrackId, name: String, add: Track.(NotationId, String) -> Track): Boolean {
        val notationName = name.trim().takeIf { it.isNotEmpty() && ',' !in it } ?: return false
        val track = readTrack(trackId) ?: return false
        if (track.namesOf().contains(notationName)) return false

        val notationId = NotationId.random()
        val sectionId = state.now.music.sectionOf(notationName)?.sectionId?.takeIf { it !in track.sections }
        updateTrack(trackId) { held ->
            val added = held.add(notationId, notationName)
            if (sectionId == null) added else added.copyTrack(sections = added.sections + (sectionId to notationId))
        }
        return true
    }

    /** Puts [notation] in the track [trackId], in place of its notation of the same id. */
    fun putNotation(trackId: TrackId, notation: Notation) = updateTrack(trackId) { it.withNotation(notation) }

    /** Removes the notation or arrangement [notationId] from the track [trackId], with the sections it played, and from its arrangements. */
    fun removeNotation(trackId: TrackId, notationId: NotationId) = updateTrack(trackId) { track ->
        val removed = track.withoutNotation(notationId)
        removed.copyTrack(arrangements = removed.arrangements.map { it.copy(notations = it.notations - notationId) })
    }

    /**
     * Names the notation or arrangement [notationId] of the track [trackId] [name]. A blank name, a name with a comma,
     * or one another of the track's holds leaves it as it is.
     */
    fun renameNotation(trackId: TrackId, notationId: NotationId, name: String) = updateTrack(trackId) { track ->
        val notationName = name.trim().takeIf { it.isNotEmpty() && ',' !in it } ?: return@updateTrack track
        val held = track.notations.filter { it.notationId != notationId }.mapNotNull { it.name } +
            track.arrangements.filter { it.notationId != notationId }.map { it.name }
        if (notationName in held) return@updateTrack track
        track.notations.firstOrNull { it.notationId == notationId }?.let { notation ->
            return@updateTrack track.withNotation(notation.withName(notationName))
        }
        track.copyTrack(arrangements = track.arrangements.map { if (it.notationId == notationId) it.copy(name = notationName) else it })
    }

    /**
     * Sets the sections the notation or arrangement [notationId] of the track [trackId] plays, taking each from any
     * other of the track's that played it.
     */
    fun setNotationSections(trackId: TrackId, notationId: NotationId, sectionIds: List<SectionId>) = updateTrack(trackId) { track ->
        val kept = track.sections.filter { (sectionId, held) -> held != notationId && sectionId !in sectionIds }
        track.copyTrack(sections = kept + sectionIds.associateWith { notationId })
    }

    /** Sets the notations the arrangement [notationId] of the track [trackId] plays, in order. */
    fun setArrangement(trackId: TrackId, notationId: NotationId, notations: List<NotationId>) = updateTrack(trackId) { track ->
        track.copyTrack(arrangements = track.arrangements.map {
            if (it.notationId == notationId) it.copy(notations = notations) else it
        })
    }

    /** Sings the melody track [trackId] with the lyric track [lyrics], or the first of the music when `null`. */
    fun setTrackLyrics(trackId: TrackId, lyrics: TrackId?) = updateTrack(trackId) { track ->
        (track as? MelodyTrack)?.copy(lyrics = lyrics) ?: track
    }

    /** Sets the sections every track follows, in order. */
    fun setMusicArrangement(sections: List<SectionId>) = state.update { it.copy(music = it.music.copy(arrangement = sections)) }

    private fun updateTrack(trackId: TrackId, transform: (Track) -> Track) = state.update { editor ->
        editor.copy(tracks = editor.tracks.map { if (it.trackId == trackId) transform(it) else it })
    }

    // saves each change once edits pause, and the last one unsaved as the editor closes
    private suspend fun saveSheetChanges() {
        var saved = sheetState.now
        try {
            sheetState.flow.debounce(SAVE_DELAY_MILLIS).collect { sheet ->
                if (sheet == saved) return@collect
                db.music.saveMusicSheet(sheet)
                saved = sheet
            }
        } finally {
            val sheet = sheetState.now
            if (sheet != saved) withContext(NonCancellable) { db.music.saveMusicSheet(sheet) }
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

/** The sections of this music [track] plays [notationId] for, in the order it states them. */
private fun Music.sectionsOf(track: Track, notationId: NotationId) =
    track.sectionIdsOf(notationId).mapNotNull { sectionId -> sections.firstOrNull { it.sectionId == sectionId } }

/** The names of this track's notations and arrangements. */
private fun Track.namesOf() = notations.mapNotNull { it.name } + arrangements.map { it.name }

/** This notation named [name]. */
private fun Notation.withName(name: String): Notation = when (this) {
    is Progression -> copy(name = name)
    is Phrase -> copy(name = name)
    is Stanza -> copy(name = name)
    is Groove -> copy(name = name)
}

data class MusicEditorState(
    val music: Music,
    val tracks: List<Track> = emptyList(),
    val focus: TrackId? = null,
)

private const val SAVE_DELAY_MILLIS = 1_000L

private fun Int.toBeatsText() = (toDouble() / Beats.TICKS_PER_BEAT).let {
    if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
}

/** [base], or [base] with the first number from 2 that no track of these holds. */
private fun List<Track>.newTrackName(base: String) = freeTrackNameOf(base, mapTo(mutableSetOf()) { it.name })
