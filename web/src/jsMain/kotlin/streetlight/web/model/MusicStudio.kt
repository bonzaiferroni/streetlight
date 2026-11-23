package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.ModelScope
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import streetlight.model.music.Chroma
import streetlight.model.music.Key
import streetlight.model.music.Mode
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.MusicSheet
import streetlight.model.music.MusicStudy
import streetlight.model.music.Song
import streetlight.model.music.SongId
import streetlight.model.music.SongMusic
import streetlight.model.music.TimeSignature
import streetlight.model.music.text.SongMarkdown
import streetlight.model.studio.StudioDb

/**
 * The musics kept in [db], each with its song, and the sheet of the one in focus, which opens the editors of it.
 * Problems go to [messenger].
 */
class MusicStudio(
    scope: CoroutineScope,
    initialMusicId: MusicId?,
    private val db: StudioDb,
    private val messenger: Messenger,
) {
    private val scope = ModelScope(scope, this::class)
    private val state = storeOf(MusicStudioState())

    val songsState = state.tapOf { it.songs }
    val focusState = state.tapOf { it.focus }
    val studiesState = state.tapOf { it.studies }

    init {
        setFocus(initialMusicId)
        scope.launch {
            db.music.flowSongMusics().collect { songs ->
                state.set { copy(songs = songs) }
            }
        }
        scope.launch {
            db.study.flowMusicStudies().collect { studies ->
                state.set { copy(studies = studies) }
            }
        }
    }

    fun setFocus(musicId: MusicId?) {
        scope.cancelAndLaunch("setFocus") {
            val sheet = musicId?.let { db.music.readMusicSheet(musicId) }
            state.set { copy(focus = sheet?.let { MusicStudioFocus.Music(it) }) }
        }
    }

    /** Stores a new song titled [title] with a music of the same name, returning the music's id. */
    suspend fun create(title: String): MusicId {
        val song = Song(SongId.random(), title, "")
        val music = newMusicOf(song)
        db.song.saveSong(song)
        db.music.saveMusic(music)
        return music.musicId
    }

    /**
     * Stores a new song and its music read from the markdown [text], titled for [fileName] when it names no title,
     * returning the music's id, or gives the problem that kept it from being read.
     */
    suspend fun importSong(text: String, fileName: String): Outcome<MusicId> {
        val song = Song(SongId.random(), fileName.substringBeforeLast('.'), "")
        val read = SongMarkdown.read(text, song, newMusicOf(song)).toDataOr { return it }

        db.song.saveSong(read.song)
        db.music.saveMusicSheet(read.sheet)
        return Ok(read.sheet.music.musicId)
    }

    /** Removes the music of [songMusic], and its song when the song has no other music. Clips stay in the library. */
    fun remove(songMusic: SongMusic) {
        val songId = songMusic.song.songId
        val musicId = songMusic.music.musicId
        scope.launch {
            val musics = db.music.flowMusics(songId).first()
            if (musics.all { it.musicId == musicId }) db.song.removeSong(songId)
            else db.music.removeMusic(musicId)
        }
    }

    /** Downloads [sheet] as markdown under its song's title and artist, written in the notation style. */
    suspend fun exportMarkdown(sheet: MusicSheet) {
        val song = db.song.flowSong(sheet.music.songId).first() ?: return
        exportMarkdown(song, sheet)
    }
}

data class MusicStudioState(
    val focus: MusicStudioFocus? = null,
    val songs: List<SongMusic> = emptyList(),
    val studies: List<MusicStudy> = emptyList(),
)

sealed interface MusicStudioFocus {
    data class Music(val sheet: MusicSheet) : MusicStudioFocus
    data class Study(val musicStudy: MusicStudy) : MusicStudioFocus
}

/** A music of [song] named for its title, in C major and 4/4. */
private fun newMusicOf(song: Song) = Music(
    musicId = MusicId.random(),
    songId = song.songId,
    name = song.title,
    key = Key(Chroma.of(0), Mode.Major),
    time = TimeSignature(4, 4),
)
