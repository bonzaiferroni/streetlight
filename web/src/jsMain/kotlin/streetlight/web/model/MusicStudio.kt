package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Tap
import kampfire.model.storeOf
import kampfire.model.tapIn
import kampfire.model.toDataOr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import streetlight.model.music.Chroma
import streetlight.model.music.Key
import streetlight.model.music.Mode
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.MusicSheet
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
    private val scope: CoroutineScope,
    initialMusicId: MusicId?,
    private val db: StudioDb,
    private val messenger: Messenger,
    private val takeShelf: TakeShelf,
) {
    private val musicIdStore = storeOf(initialMusicId)
    private val sheetStore = storeOf<MusicSheet?>(null)

    /** Each music kept, with its song. */
    val songMusicsState: Tap<List<SongMusic>> = db.music.flowSongMusics().tapIn(scope, emptyList())

    /** The sheet of the music in focus, read as the focus moves to it, or `null` for none or one not kept. */
    val sheetState: Tap<MusicSheet?> = sheetStore

    init {
        scope.launch {
            musicIdStore.flow.collectLatest { musicId ->
                sheetStore.set(musicId?.let { db.music.readMusicSheet(it) })
            }
        }
    }

    fun setMusicId(musicId: MusicId?) = musicIdStore.set(musicId)

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

/** A music of [song] named for its title, in C major and 4/4. */
private fun newMusicOf(song: Song) = Music(
    musicId = MusicId.random(),
    songId = song.songId,
    name = song.title,
    key = Key(Chroma.of(0), Mode.Major),
    time = TimeSignature(4, 4),
)
