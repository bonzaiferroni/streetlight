package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import streetlight.model.data.Beats
import streetlight.model.data.lengthOf
import kotlinx.serialization.Serializable
import streetlight.model.data.Chroma
import streetlight.model.data.Key
import streetlight.model.data.Mode
import streetlight.model.data.Music
import streetlight.model.data.MusicId
import streetlight.model.data.NotationStyle
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.model.data.TimeSignature
import streetlight.model.data.Track

/** The state behind the music editor, holding the song loaded from [db] and saving each change to it. */
class MusicEditor(
    scope: CoroutineScope,
    private val db: SongDb,
    private val player: TrackPlayer,
) {
    private val state = storeOf<MusicEditorState?>(null)
    val songState = state.tapOf { it?.song }
    val musicState = state.mutableTapOf({ it?.music }) { music -> if (this == null || music == null) this else copy(music = music) }
    val notationStyleState = state.mutableTapOf({ it?.notationStyle ?: NotationStyle.Letters }) {
        this?.copy(notationStyle = it)
    }
    val tracksState = state.tapOf { it?.tracks.orEmpty() }
    val focusTrackState = state.tapOf { it?.focusTrack }
    val songsState = db.songsState

    val isPlayingState = player.isPlayingState

    init {
        state.reactIn(scope) { song -> song?.let { db.save(it) } }
        // playback left running when the editor closes would keep sounding
        scope.coroutineContext[Job]?.invokeOnCompletion { player.stop() }
    }

    /** Plays the track at [index] alone through the song's arrangement. */
    fun playTrack(index: Int) {
        val song = state.now ?: return
        val track = song.tracks.getOrNull(index) ?: return
        player.play(song.music, song.tracks, listOf(track))
    }

    /** Plays every track of the song together through its arrangement. */
    fun playSong() {
        val song = state.now ?: return
        player.play(song.music, song.tracks, song.tracks)
    }

    fun stop() = player.stop()

    /** The warnings on the track at [index]: each section it defines that runs shorter than in another track. */
    fun sectionWarningsState(index: Int) = state.tapOf { song ->
        val tracks = song?.tracks.orEmpty()
        val track = tracks.getOrNull(index) ?: return@tapOf emptyList()
        val ids = track.sections.map { it.sectionId } + track.arrangements.map { it.sectionId }
        ids.mapNotNull { sectionId ->
            val length = track.lengthOf(sectionId) ?: return@mapNotNull null
            val longest = tracks.lengthOf(sectionId) ?: return@mapNotNull null
            val shortfall = longest.ticks - length.ticks
            if (shortfall <= 0) null
            else "${sectionId.name} runs ${shortfall.toBeatsText()} beats shorter than in another track"
        }
    }

    fun load(songId: SongId) = state.set(db.read(songId))

    /** Stores a new song titled [title] and loads it. */
    fun create(title: String) = state.set(newMusicEditorState(title))

    fun readTrack(index: Int) = state.now?.tracks?.getOrNull(index)

    /** The track at [index] of the loaded song. */
    fun trackState(index: Int) = state.mutableTapOf({ it?.tracks?.getOrNull(index) }) { track ->
        if (this == null || track == null) this
        else copy(tracks = tracks.toMutableList().apply { set(index, track) })
    }

    /**
     * The track at [index] of the loaded song as a [T], reading [empty] while there is no such track.
     *
     * A removal or unload can reach the lens before the view reading it is rebuilt, which then reads [empty].
     */
    inline fun <reified T : Track> trackState(index: Int, empty: T): MutableTap<T> =
        trackState(index).mutableTapOf({ it as? T ?: empty }) { it }

    /** Adds [track] to the loaded song and focuses it. */
    fun addTrack(track: Track) = state.update { song ->
        song?.copy(tracks = song.tracks + track, focusTrack = song.tracks.size)
    }

    fun focusTrack(index: Int?) = state.update { it?.copy(focusTrack = index) }

    /** Removes the track at [index], keeping the focus on the track it was on. */
    fun removeTrack(index: Int) = state.update { song ->
        song?.copy(
            tracks = song.tracks.filterIndexed { i, _ -> i != index },
            focusTrack = song.focusTrack?.let { focus ->
                when {
                    focus == index -> null
                    focus > index -> focus - 1
                    else -> focus
                }
            },
        )
    }

    /** Removes the song of [songId], unloading it if loaded. */
    fun remove(songId: SongId) {
        db.remove(songId)
        if (state.now?.song?.songId == songId) state.set(null)
    }
}

@Serializable
data class MusicEditorState(
    val song: Song,
    val music: Music,
    val tracks: List<Track> = emptyList(),
    val notationStyle: NotationStyle = NotationStyle.Letters,
    val focusTrack: Int? = null,
)

private fun newMusicEditorState(title: String): MusicEditorState {
    val songId = SongId.random()
    return MusicEditorState(
        song = Song(songId, title, ""),
        music = Music(
            musicId = MusicId.random(),
            songId = songId,
            key = Key(Chroma.of(0), Mode.Major),
            time = TimeSignature(4, 4),
        ),
    )
}

private fun Int.toBeatsText() = (toDouble() / Beats.TICKS_PER_BEAT).let {
    if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
}
