package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import streetlight.model.music.Mix
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.MusicSheet
import streetlight.model.music.Section
import streetlight.model.music.Track
import streetlight.model.music.TrackSectionId
import streetlight.model.music.TrackType
import streetlight.model.studio.StudioDb

class MixEditor(
    private val scope: CoroutineScope,
    initialMix: Mix,
    private val sheetState: Tap<MusicSheet>,
    private val db: StudioDb,
) {
    private val state = storeOf(MixEditorState(initialMix))

    val focusState = state.tapOf { it.focus }
    val mixState = state.tapOf { it.mix }
    val sectionsState = sheetState.tapOf { sheet ->
        sheet.tracks.flatMap { track ->
            track.sections.map { section ->
                trackSectionOf(track, section, sheet.music)
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