package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.web.utility.downloadFile
import streetlight.model.data.Beats
import streetlight.model.data.ChordTrack
import streetlight.model.data.DrumTrack
import streetlight.model.data.LyricTrack
import streetlight.model.data.MelodyTrack
import streetlight.model.data.lengthOf
import streetlight.model.data.repeatsOf
import streetlight.model.data.Music
import streetlight.model.data.NotationStyle
import streetlight.model.data.Track

/** The state behind the editor of the music and tracks of [stored], written in the style at [notationStyleState]. */
class MusicEditor(
    scope: CoroutineScope,
    stored: StoredSong,
    val notationStyleState: Tap<NotationStyle>,
) {
    private val state = storeOf(MusicEditorState(stored.music, stored.tracks))
    val song = stored.song
    val musicState = state.mutableTapOf({ it.music }) { copy(music = it) }
    val tracksState = state.tapOf { it.tracks }
    val trackLabelsState = state.tapOf { editor -> editor.tracks.map { it.label } }

    /** The song as it is kept, following each change to its music and tracks. */
    val storedState = state.tapOf { StoredSong(song, it.music, it.tracks) }

    /** The text of the song's music. */
    val properties = MusicPropertyEditor(scope, musicState)

    /** Hands the viewer the song's tracks as a MIDI file, laid along its arrangement. */
    fun exportMidi() {
        val editor = state.now
        val trackTones = songTonesOf(editor.music, editor.tracks, editor.tracks).filter { it.isSounding }
        downloadFile(writeMidi(editor.music, trackTones), "${song.title}.mid", MIDI_TYPE)
    }

    /** The warnings on the track at [index]: each section it defines whose repeats fall short of another track's. */
    fun sectionWarningsState(index: Int) = state.tapOf { editor ->
        val tracks = editor.tracks
        val track = tracks.getOrNull(index) ?: return@tapOf emptyList()
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

    /** The track at [index]. */
    fun trackState(index: Int) = state.mutableTapOf({ it.tracks.getOrNull(index) }) { track ->
        if (track == null) this
        else copy(tracks = tracks.toMutableList().apply { set(index, track) })
    }

    /**
     * The track at [index] as a [T], reading [empty] while there is no such track.
     *
     * A removal can reach the lens before the view reading it is rebuilt, which then reads [empty].
     */
    inline fun <reified T : Track> trackState(index: Int, empty: T): MutableTap<T> =
        trackState(index).mutableTapOf({ it as? T ?: empty }) { it }

    /** Whether the editor of the track at [index] is open. */
    fun isTrackOpenState(index: Int) = state.tapOf { index in it.openTracks }

    /** Adds [track] to the song and opens its editor. */
    fun addTrack(track: Track) = state.update { editor ->
        editor.copy(tracks = editor.tracks + track, openTracks = editor.openTracks + editor.tracks.size)
    }

    /** Opens the editor of the track at [index], or closes it when open. */
    fun toggleTrack(index: Int) = state.update { editor ->
        editor.copy(openTracks = if (index in editor.openTracks) editor.openTracks - index else editor.openTracks + index)
    }

    /** Names the track at [index] [name], or clears its name when [name] is blank. */
    fun renameTrack(index: Int, name: String) = state.update { editor ->
        val track = editor.tracks.getOrNull(index) ?: return@update editor
        val trackName = name.trim().ifEmpty { null }
        val renamed = when (track) {
            is ChordTrack -> track.copy(name = trackName)
            is MelodyTrack -> track.copy(name = trackName)
            is LyricTrack -> track.copy(name = trackName)
            is DrumTrack -> track.copy(name = trackName)
        }
        editor.copy(tracks = editor.tracks.toMutableList().apply { set(index, renamed) })
    }

    /** Removes the track at [index], keeping open the editors of the tracks after it. */
    fun removeTrack(index: Int) = state.update { editor ->
        editor.copy(
            tracks = editor.tracks.filterIndexed { i, _ -> i != index },
            openTracks = editor.openTracks.mapNotNull { open ->
                when {
                    open == index -> null
                    open > index -> open - 1
                    else -> open
                }
            }.toSet(),
        )
    }
}

data class MusicEditorState(
    val music: Music,
    val tracks: List<Track> = emptyList(),
    val openTracks: Set<Int> = emptySet(),
)

private fun Int.toBeatsText() = (toDouble() / Beats.TICKS_PER_BEAT).let {
    if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
}

/** The track's name, or the name of its type when it has none. */
val Track.label get() = name ?: when (this) {
    is ChordTrack -> "Chords"
    is MelodyTrack -> "Melody"
    is LyricTrack -> "Lyrics"
    is DrumTrack -> "Drums"
}

private const val MIDI_TYPE = "audio/midi"
