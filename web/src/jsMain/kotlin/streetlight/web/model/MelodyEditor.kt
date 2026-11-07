package streetlight.web.model

import kampfire.model.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.model.music.MelodyTrack
import streetlight.model.music.TrackId
import streetlight.model.music.Phrase
import streetlight.model.music.text.MelodyParser
import streetlight.model.music.ui.sectionNotationsOf

/**
 * The state behind the editor of the melody track [trackId] of [model], read from jianpu. A read replaces the track's
 * sections, arrangements and capo, and leaves the rest of it alone. A change of key or time rewrites the text from the
 * track.
 */
class MelodyEditor(
    scope: CoroutineScope,
    private val model: MusicEditor,
    trackId: TrackId,
) {
    private val trackState = model.trackState(trackId, MelodyTrack(trackId, emptyList(), emptyList()))
    private val field = NotationField(writeNow())
    private var readSections: List<Phrase>? = null
    val textState = field.textState
    val errorsState = field.errorsState

    /** The notation of each section of the track. */
    val sectionsState = trackState.combine(model.musicState) { track, music -> sectionNotationsOf(track, music) }

    init {
        // a change of key or time changes how the track is written, so the text follows the track
        val changes = model.musicState.flow.map { music -> music.key to music.time }.distinctUntilChanged()
        field.launchRewrites(scope, changes) { writeNow() }
        // an edit from elsewhere replaces the track under the text, which a later read would otherwise undo
        scope.launch {
            trackState.flow.filter { readSections != null && it.sections != readSections }.collect {
                field.setText(writeNow())
            }
        }
        field.launchReads(scope) { text ->
            val music = model.musicState.now
            val read = MelodyParser(music.key).read(trackId, text, music.time)
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

    private fun writeNow() = model.musicState.now.let { MelodyParser(it.key).write(trackState.now, it.time) }
}
