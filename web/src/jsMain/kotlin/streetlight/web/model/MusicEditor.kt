package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import streetlight.model.music.Beats
import streetlight.model.music.ChordTrack
import streetlight.model.music.DrumTrack
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Music
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.freeTrackIdOf
import streetlight.model.music.lengthOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.text.NotationStyle

/**
 * The state behind the editor of the music and tracks of [stored], written in the style at [notationStyleState], a
 * melody's notation showing its lyrics while [isLyricsShownState].
 */
class MusicEditor(
    private val scope: CoroutineScope,
    stored: StoredSong,
    val notationStyleState: Tap<NotationStyle>,
    val isLyricsShownState: MutableTap<Boolean>,
) {
    private val state = storeOf(MusicEditorState(stored.music, stored.tracks))
    val song = stored.song
    val musicState = state.mutableTapOf({ it.music }) { copy(music = it) }
    val tracksState = state.mutableTapOf({ it.tracks }) { copy(tracks = it) }

    /** The song as it is kept, following each change to its music and tracks. */
    val storedState = state.tapOf { StoredSong(song, it.music, it.tracks) }

    private val trackChangeFlow = MutableSharedFlow<TrackChange>(extraBufferCapacity = TRACK_CHANGE_BUFFER)

    /** Each track renamed or removed, as it happens. */
    val trackChanges: SharedFlow<TrackChange> = trackChangeFlow

    /** The text of the song's music. */
    val properties = MusicPropertyEditor(scope, musicState)

    /** The warnings on the track [trackId]: each section it defines whose repeats fall short of another track's. */
    fun sectionWarningsState(trackId: TrackId) = state.tapOf { editor ->
        val tracks = editor.tracks
        val track = tracks.firstOrNull { it.trackId == trackId } ?: return@tapOf emptyList()
        val ids = track.sections.map { it.sectionId } + track.arrangements.map { it.sectionId }
        ids.mapNotNull { sectionId ->
            val length = track.lengthOf(sectionId) ?: return@mapNotNull null
            val longest = tracks.lengthOf(sectionId) ?: return@mapNotNull null
            val shortfall = longest.ticks - length.ticks * track.repeatsOf(sectionId, longest)
            if (shortfall <= 0) null
            else "${sectionId.name} falls ${shortfall.toBeatsText()} beats short of another track after its repeats"
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

    /**
     * The track [trackId] as a [T], reading [empty] while there is no such track.
     *
     * A removal can reach the lens before the view reading it is rebuilt, which then reads [empty].
     */
    inline fun <reified T : Track> trackState(trackId: TrackId, empty: T): MutableTap<T> =
        trackState(trackId).mutableTapOf({ it as? T ?: empty }) { it }

    /** Whether the editor of the track [trackId] is open. */
    fun isTrackOpenState(trackId: TrackId) = state.tapOf { trackId in it.openTracks }

    /** A name for a new track that no track of the song holds: [base], or [base] with the first number that frees it. */
    fun newTrackId(base: String) = state.now.tracks.newTrackId(base)

    /** Adds [track] to the song and opens its editor. */
    fun addTrack(track: Track) = state.update { editor ->
        editor.copy(tracks = editor.tracks + track, openTracks = editor.openTracks + track.trackId)
    }

    /** Opens the editor of the track [trackId], or closes it when open. */
    fun toggleTrack(trackId: TrackId) = state.update { editor ->
        val openTracks = if (trackId in editor.openTracks) editor.openTracks - trackId else editor.openTracks + trackId
        editor.copy(openTracks = openTracks)
    }

    /**
     * Names the track [trackId] [name], numbered when another track holds it. A blank name leaves the track as it
     * is.
     */
    fun renameTrack(trackId: TrackId, name: String) {
        var change: TrackChange? = null
        state.update { editor ->
            val track = editor.tracks.firstOrNull { it.trackId == trackId } ?: return@update editor
            val base = name.trim().takeIf { it.isNotEmpty() && it != trackId.name } ?: return@update editor
            val renamedId = editor.tracks.filter { it.trackId != trackId }.newTrackId(base)
            val renamed = when (track) {
                is ChordTrack -> track.copy(trackId = renamedId)
                is MelodyTrack -> track.copy(trackId = renamedId)
                is LyricTrack -> track.copy(trackId = renamedId)
                is DrumTrack -> track.copy(trackId = renamedId)
            }
            editor.copy(
                tracks = editor.tracks.map { if (it.trackId == trackId) renamed else it },
                openTracks = if (trackId in editor.openTracks) editor.openTracks - trackId + renamedId else editor.openTracks,
            ).also { change = TrackChange.Renamed(trackId, renamedId) }
        }
        change?.let { trackChangeFlow.tryEmit(it) }
    }

    /** Removes the track [trackId]. */
    fun removeTrack(trackId: TrackId) {
        if (readTrack(trackId) == null) return
        state.update { editor ->
            editor.copy(
                tracks = editor.tracks.filter { it.trackId != trackId },
                openTracks = editor.openTracks - trackId,
            )
        }
        trackChangeFlow.tryEmit(TrackChange.Removed(trackId))
    }

}

data class MusicEditorState(
    val music: Music,
    val tracks: List<Track> = emptyList(),
    val openTracks: Set<TrackId> = emptySet(),
)

/** A track renamed or removed, which a reading of the tracks alone can't tell. */
sealed interface TrackChange {
    /** The track [from] now goes by [to]. */
    data class Renamed(val from: TrackId, val to: TrackId) : TrackChange

    /** The track [trackId] is gone. */
    data class Removed(val trackId: TrackId) : TrackChange
}

// the changes one edit can make, for a collector that falls behind
private const val TRACK_CHANGE_BUFFER = 64

private fun Int.toBeatsText() = (toDouble() / Beats.TICKS_PER_BEAT).let {
    if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
}

/** [base], or [base] with the first number from 2 that no track of these holds. */
private fun List<Track>.newTrackId(base: String) = freeTrackIdOf(base, mapTo(mutableSetOf()) { it.trackId })
