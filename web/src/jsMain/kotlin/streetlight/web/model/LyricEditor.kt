package streetlight.web.model

import kampfire.model.combine
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.LyricParser
import streetlight.model.data.LyricTrack

/**
 * The state behind the editor of the lyric track at [index] of [model]. A read replaces the track's sections,
 * arrangements and capo, and leaves the rest of it alone.
 */
class LyricEditor(
    scope: CoroutineScope,
    private val model: MusicEditor,
    index: Int,
) {
    private val trackState = model.trackState(index, LyricTrack(emptyList(), emptyList()))
    private val field = NotationField(model.musicState.now?.let { LyricParser.write(trackState.now, it.time) })
    val textState = field.textState
    val errorsState = field.errorsState

    /** The track written as the text it reads from, or `null` while there is no music. */
    val notationState = trackState.combine(model.musicState) { track, music ->
        music?.let { LyricParser.write(track, it.time) }
    }

    init {
        field.launchReads(scope, model.musicState.flow) { text ->
            val music = model.musicState.now ?: return@launchReads emptyList()
            val read = LyricParser.read(text, music.time)
            trackState.update { track ->
                track.copy(
                    sections = read.value.sections,
                    arrangements = read.value.arrangements,
                    capo = read.value.capo,
                )
            }
            read.errors
        }
    }
}
