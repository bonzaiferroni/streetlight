package streetlight.web.model

import kampfire.model.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import streetlight.model.music.LyricTrack
import streetlight.model.music.TrackId
import streetlight.model.music.MelodyTrack
import streetlight.model.music.text.LyricParser
import streetlight.model.music.ui.sectionNotationsOf

/**
 * The state behind the editor of the lyric track [trackId] of [model]. A read replaces the track's sections,
 * arrangements and capo, and leaves the rest of it alone. A read is checked against the song's first melody track. A
 * change of time rewrites the text from the track.
 */
class LyricEditor(
    scope: CoroutineScope,
    private val model: MusicEditor,
    trackId: TrackId,
) {
    private val trackState = model.trackState(trackId, LyricTrack(trackId, emptyList(), emptyList()))
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
            val melody = model.tracksState.now.firstNotNullOfOrNull { it as? MelodyTrack }
            val read = LyricParser.read(trackId, text, music.time, melody)
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

    private fun writeNow() = model.musicState.now.let { LyricParser.write(trackState.now, it.time) }
}
