package streetlight.web.model

import kampfire.model.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import streetlight.model.data.ChordParser
import streetlight.model.data.ChordTrack
import streetlight.model.data.Progression

/**
 * The state behind the editor of the chord track at [index] of [model], read from chord symbols in the song's
 * notation style. A read replaces the track's sections, arrangements and capo, and leaves the rest of it alone. A
 * change of key, time or style rewrites the text from the track.
 */
class ChordEditor(
    scope: CoroutineScope,
    private val model: MusicEditor,
    index: Int,
) {
    private val trackState = model.trackState(index, ChordTrack(emptyList(), emptyList()))
    private val field = NotationField(writeNow())
    private var readSections: List<Progression>? = null
    val textState = field.textState
    val errorsState = field.errorsState

    /** The notation of each section of the track. */
    val sectionsState = trackState.combine(model.musicState) { track, music -> sectionNotationsOf(track, music) }

    init {
        // a change of key transposes the track, and a change of time or style respells it, so the text follows it
        val changes = combine(model.musicState.flow, model.notationStyleState.flow) { music, style ->
            Triple(music.key, music.time, style)
        }
        field.launchRewrites(scope, changes.distinctUntilChanged()) { writeNow() }
        // an edit from elsewhere replaces the track under the text, which a later read would otherwise undo
        scope.launch {
            trackState.flow.filter { readSections != null && it.sections != readSections }.collect {
                field.setText(writeNow())
            }
        }
        field.launchReads(scope) { text ->
            val music = model.musicState.now
            val read = ChordParser(music.key, model.notationStyleState.now).read(text, music.time)
            readSections = read.value.sections
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

    private fun writeNow() = model.musicState.now.let { music ->
        ChordParser(music.key, model.notationStyleState.now).write(trackState.now, music.time)
    }
}
