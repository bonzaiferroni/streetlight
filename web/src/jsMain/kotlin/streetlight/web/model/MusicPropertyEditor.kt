package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Music
import streetlight.model.data.MusicParser

/** The text of the key, time, tempo, capo and arrangement of the music at [musicState], and the problems found reading it. */
class MusicPropertyEditor(
    scope: CoroutineScope,
    private val musicState: MutableTap<Music>,
) {
    private val field = NotationField(MusicParser.write(musicState.now))
    val textState = field.textState
    val errorsState = field.errorsState

    /** The music written as the text it reads from. */
    val notationState = musicState.tapOf { MusicParser.write(it) }

    init {
        field.launchReads(scope) { text ->
            val read = MusicParser.read(text, musicState.now)
            musicState.set(read.value)
            read.errors
        }
    }
}
