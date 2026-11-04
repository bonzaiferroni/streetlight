package streetlight.web.model

import kampfire.model.mutableTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
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
) {
    private val state = storeOf<MusicEditorState?>(null)
    val songState = state.tapOf { it?.song }
    val musicState = state.tapOf { it?.music }
    val notationStyleState = state.mutableTapOf({ it?.notationStyle ?: NotationStyle.Letters }) {
        this?.copy(notationStyle = it)
    }
    val songsState = db.songsState

    init {
        state.reactIn(scope) { song -> song?.let { db.save(it) } }
    }

    fun load(songId: SongId) = state.set(db.read(songId))

    /** Stores a new song titled [title] and loads it. */
    fun create(title: String) = state.set(newMusicEditorState(title))

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
