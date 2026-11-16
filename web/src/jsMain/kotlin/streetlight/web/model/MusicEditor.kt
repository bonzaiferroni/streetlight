package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Music
import streetlight.model.music.MusicSheet
import streetlight.model.music.Section
import streetlight.model.music.SectionId
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.TrackType
import streetlight.model.music.copyTrack
import streetlight.model.music.emptyTrackOf
import streetlight.model.music.freeTrackNameOf
import streetlight.model.studio.StudioDb

/**
 * The state behind the editor of the music and tracks of [initialSheet]. The music is saved to [db] once edits pause,
 * and the tracks follow [db], where the editor of a track in focus saves it.
 */
class MusicEditor(
    private val scope: CoroutineScope,
    initialSheet: MusicSheet,
    private val db: StudioDb,
) {
    private val state = storeOf(MusicEditorState(initialSheet.music, initialSheet.tracks))

    val musicState = state.mutableTapOf({ it.music }) { copy(music = it) }
    val sheetState: Tap<MusicSheet> = state.tapOf { MusicSheet(it.music, it.tracks) }
    val focusState: Tap<MusicEditorFocus?> = state.tapOf { it.focus }
    val lyricTracksState: Tap<List<LyricTrack>> = state.tapOf { editor -> editor.tracks.filterIsInstance<LyricTrack>() }
    val sectionsState: Tap<List<Section>> = state.tapOf { it.music.sections }
    val arrangementState: Tap<List<Section>> = state.tapOf { editor ->
        editor.music.arrangement.mapNotNull { sectionId -> editor.music.sections.firstOrNull { it.sectionId == sectionId } }
    }

    /** The tracks in their order; setting them reorders the music's tracks. */
    val tracksState = state.mutableTapOf({ it.tracks }) { tracks ->
        copy(tracks = tracks, music = music.copy(trackOrder = tracks.map { it.trackId }))
    }

    init {
        scope.launch { musicState.saveChanges { db.music.saveMusic(it) } }
        scope.launch {
            db.track.flowTracks(initialSheet.music.musicId).collect { tracks ->
                state.update { it.copy(tracks = it.music.orderTracks(tracks)) }
            }
        }
    }

    fun readTrack(trackId: TrackId) = state.now.tracks.firstOrNull { it.trackId == trackId }

    fun trackState(trackId: TrackId): Tap<Track?> = state.tapOf { editor -> editor.tracks.firstOrNull { it.trackId == trackId } }

    /** Shows [focus] in place of the overview, or the overview for `null`. */
    fun setFocus(focus: MusicEditorFocus?) = state.update { it.copy(focus = focus) }

    /** Adds an empty track of [trackType], named for its type under a name no track holds, and brings it into focus. */
    fun addTrack(trackType: TrackType) {
        val editor = state.now
        val track = emptyTrackOf(trackType, editor.music.musicId, editor.tracks.newTrackName(trackType.name))
        state.update { it.copy(music = it.music.copy(trackOrder = it.music.trackOrder + track.trackId), focus = TrackFocus(track)) }
        scope.launch { db.track.saveTrack(track) }
    }

    /**
     * Names the track [trackId] [name], numbered when another track holds it, sets its [capo], and sings a melody with
     * the lyric track [lyrics], or the first of the music when `null`. A blank name keeps the one it has.
     */
    fun editTrack(trackId: TrackId, name: String, capo: Int?, lyrics: TrackId?) {
        val tracks = state.now.tracks
        val track = tracks.firstOrNull { it.trackId == trackId } ?: return
        val base = name.trim().takeIf { it.isNotEmpty() && it != track.name }
        val freeName = base?.let { tracks.filter { it.trackId != trackId }.newTrackName(it) } ?: track.name
        val edited = track.copyTrack(name = freeName, capo = capo).let { (it as? MelodyTrack)?.copy(lyrics = lyrics) ?: it }
        scope.launch { db.track.saveTrack(edited) }
    }

    /** Removes the track [trackId] with its bound clips and its studies. */
    fun removeTrack(trackId: TrackId) {
        state.update { editor ->
            editor.copy(
                music = editor.music.copy(trackOrder = editor.music.trackOrder - trackId),
                focus = editor.focus?.takeIf { it.track.trackId != trackId },
            )
        }
        scope.launch { db.track.removeTrack(trackId) }
    }

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

    /** Sets the sections every track follows, in order. */
    fun setMusicArrangement(sections: List<SectionId>) = state.update { it.copy(music = it.music.copy(arrangement = sections)) }
}

data class MusicEditorState(
    val music: Music,
    val tracks: List<Track> = emptyList(),
    val focus: MusicEditorFocus? = null,
)

/** [base], or [base] with the first number from 2 that no track of these holds. */
private fun List<Track>.newTrackName(base: String) = freeTrackNameOf(base, mapTo(mutableSetOf()) { it.name })
