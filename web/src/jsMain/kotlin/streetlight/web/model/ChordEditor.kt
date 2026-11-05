package streetlight.web.model

import kampfire.model.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import streetlight.model.data.ChordParser
import streetlight.model.data.ChordTrack

/**
 * The state behind the editor of the chord track at [index] of [model], read from chord symbols in the song's
 * notation style. A read replaces the track's sections, arrangements and capo, and leaves the rest of it alone.
 */
class ChordEditor(
    scope: CoroutineScope,
    private val model: MusicEditor,
    index: Int,
) {
    private val trackState = model.trackState(index, ChordTrack(emptyList(), emptyList()))
    private val field = NotationField(writeNow())
    val textState = field.textState
    val errorsState = field.errorsState

    /** The track written as the text it reads from, or `null` while there is no music. */
    val notationState = trackState
        .combine(model.musicState) { track, music -> music?.let { track to it } }
        .combine(model.notationStyleState) { pair, style ->
            pair?.let { (track, music) -> ChordParser(music.key, style).write(track, music.time) }
        }

    init {
        val triggers = combine(model.musicState.flow, model.notationStyleState.flow) { music, style -> music to style }
        field.launchReads(scope, triggers) { text ->
            val music = model.musicState.now ?: return@launchReads emptyList()
            val read = ChordParser(music.key, model.notationStyleState.now).read(text, music.time)
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

    private fun writeNow() = model.musicState.now?.let { music ->
        ChordParser(music.key, model.notationStyleState.now).write(trackState.now, music.time)
    }
}
