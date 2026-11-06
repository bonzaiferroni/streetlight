package streetlight.web.model

import kampfire.model.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import streetlight.model.data.MelodyParser
import streetlight.model.data.MelodyTrack

/**
 * The state behind the editor of the melody track at [index] of [model], read from jianpu. A read replaces the track's
 * sections, arrangements and capo, and leaves the rest of it alone. A change of key or time rewrites the text from the
 * track.
 */
class MelodyEditor(
    scope: CoroutineScope,
    private val model: MusicEditor,
    index: Int,
) {
    private val trackState = model.trackState(index, MelodyTrack(emptyList(), emptyList()))
    private val field = NotationField(writeNow())
    val textState = field.textState
    val errorsState = field.errorsState

    /** The notation of each section of the track. */
    val sectionsState = trackState.combine(model.musicState) { track, music -> sectionNotationsOf(track, music.time) }

    init {
        // a change of key or time changes how the track is written, so the text follows the track
        val changes = model.musicState.flow.map { music -> music.key to music.time }.distinctUntilChanged()
        field.launchRewrites(scope, changes) { writeNow() }
        field.launchReads(scope) { text ->
            val music = model.musicState.now
            val read = MelodyParser(music.key).read(text, music.time)
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

    private fun writeNow() = model.musicState.now.let { MelodyParser(it.key).write(trackState.now, it.time) }
}
