package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import streetlight.model.music.Mix
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.Section
import streetlight.model.music.Track
import streetlight.model.music.TrackSectionId
import streetlight.model.music.TrackType
import streetlight.model.studio.StudioDb

class MixEditor(
    private val scope: CoroutineScope,
    initialMix: Mix,
    private val musicState: Tap<Music>,
    private val db: StudioDb,
) {
    private val state = storeOf(MixEditorState(initialMix))
    private val musicId = musicState.now.musicId

    val focusState = state.tapOf { it.focus }
    val mixState = state.tapOf { it.mix }
    val sectionsState = state.tapOf { it.sections }

    init {
        scope.launch {
            db.track.flowTracks(musicId).combine(musicState.flow) { tracks, music ->
                tracks.flatMap { track ->
                    track.sections.map { section ->
                        trackSectionOf(track, section, music)
                    }
                }
            }.collect {
                state.set { copy(sections = it) }
            }
        }
    }

    fun focusSection(trackSectionId: TrackSectionId?) {
        state.set { copy(focus = trackSectionId)}
    }
}

data class MixEditorState(
    val mix: Mix,
    val focus: TrackSectionId? = null,
    val sections: List<TrackSection> = emptyList(),
)

/** A section of a track as the mix tab shows it, under [label], as `Melody · Verse`. */
data class TrackSection(
    val trackSectionId: TrackSectionId,
    val label: String,
    val trackType: TrackType,
)

fun trackSectionOf(track: Track, section: Section, music: Music) =
    TrackSection(
        trackSectionId = TrackSectionId(track.trackId, section.sectionId),
        label = "${track.name} · ${music.nameOf(section.sectionId)}",
        trackType = track.trackType
    )