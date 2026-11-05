package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import streetlight.model.data.Music
import streetlight.model.data.MusicParser
import streetlight.model.data.SongId

/**
 * The text of the key, time, tempo, capo and arrangement of the music at [musicState], and the problems found reading
 * it. The text starts afresh from the music whenever [songIdState] loads another song.
 */
class MusicPropertyEditor(
    scope: CoroutineScope,
    private val musicState: MutableTap<Music?>,
    songIdState: Tap<SongId?>,
) {
    private val field = NotationField()
    val textState = field.textState
    val errorsState = field.errorsState

    /** The music written as the text it reads from, or `null` while no song is loaded. */
    val notationState = musicState.tapOf { music -> music?.let { MusicParser.write(it) } }

    init {
        scope.launch {
            songIdState.flow.distinctUntilChanged().collect {
                field.setText(musicState.now?.let { MusicParser.write(it) })
            }
        }
        field.launchReads(scope) { text ->
            val music = musicState.now ?: return@launchReads emptyList()
            val read = MusicParser.read(text, music)
            musicState.set(read.value)
            read.errors
        }
    }
}
