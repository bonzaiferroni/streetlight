package streetlight.web.model

import kampfire.model.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import streetlight.model.data.DrumParser
import streetlight.model.data.DrumTrack

/**
 * The state behind the editor of the drum track at [index] of [model]. A read replaces the track's sections,
 * arrangements and capo, and leaves the rest of it alone. A change of time rewrites the text from the track.
 */
class DrumEditor(
    scope: CoroutineScope,
    private val model: MusicEditor,
    index: Int,
) {
    private val trackState = model.trackState(index, DrumTrack(emptyList(), emptyList()))
    private val field = NotationField(writeNow())
    val textState = field.textState
    val errorsState = field.errorsState

    /** The notation of each section of the track. */
    val sectionsState = trackState.combine(model.musicState) { track, music -> sectionNotationsOf(track, music) }

    init {
        // a change of time changes how the track is written, so the text follows the track
        val changes = model.musicState.flow.map { music -> music.time }.distinctUntilChanged()
        field.launchRewrites(scope, changes) { writeNow() }
        field.launchReads(scope) { text ->
            val music = model.musicState.now
            val read = DrumParser.read(text, music.time)
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

    private fun writeNow() = model.musicState.now.let { DrumParser.write(trackState.now, it.time) }
}
