@file:OptIn(ExperimentalCoroutinesApi::class)

package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.model.music.Clip
import streetlight.model.music.MusicSheet
import streetlight.model.music.NotationId
import streetlight.model.music.TrackClip
import streetlight.model.music.TrackId
import streetlight.model.studio.StudioDb

/**
 * Edits the clip bound in [db] to the notation [notationId] of the track [trackId] in [sheetState], or to the whole
 * track when `null`, choosing among the clips of the library that fit it, or none.
 */
class ClipEditor(
    private val scope: CoroutineScope,
    private val sheetState: Tap<MusicSheet>,
    private val db: StudioDb,
    private val trackId: TrackId,
    private val notationId: NotationId?,
) {
    private val state = storeOf(ClipEditorState())
    private val musicId = sheetState.now.music.musicId

    val clipState = state.tapOf { it.clip }
    val isClipStaleState = state.tapOf { it.isClipStale }
    val isPopoverOpenState = state.mutableTapOf({ it.isPopoverOpen }) { copy(isPopoverOpen = it) }
    val clipsState = state.tapOf { it.clips }

    init {
        scope.launch {
            db.trackClip.flowTrackClips(musicId)
                .map { trackClips -> trackClips.firstOrNull { it.trackId == trackId && it.notationId == notationId }?.clipId }
                .distinctUntilChanged()
                .flatMapLatest { clipId -> clipId?.let { db.clip.flowClip(it) } ?: flowOf(null) }
                .combine(sheetState.flow) { clip, sheet -> clip to (clip != null && !fitsSheet(clip, sheet)) }
                .collect { (clip, isClipStale) -> state.update { it.copy(clip = clip, isClipStale = isClipStale) } }
        }
        // the choices are read as the popover opens, the bound clip among them whether it fits or not
        isPopoverOpenState.reactIn(scope) { isOpen ->
            if (!isOpen) return@reactIn
            val sheet = sheetState.now
            val boundId = state.now.clip?.clipId
            val clips = db.clip.flowClips().first()
                .filter { it.clipId == boundId || fitsSheet(it, sheet) }
                .sortedBy { it.name }
            state.update { it.copy(clips = clips) }
        }
    }

    /** Binds [clip], or unbinds the clip bound for `null`, so tones play. */
    fun setClip(clip: Clip?) {
        scope.launch {
            if (clip == null) db.trackClip.removeTrackClip(trackId, notationId)
            else db.trackClip.saveTrackClip(TrackClip(musicId, trackId, notationId, clip.clipId))
        }
    }

    private fun fitsSheet(clip: Clip, sheet: MusicSheet): Boolean {
        val track = sheet.tracks.firstOrNull { it.trackId == trackId } ?: return false
        return clip.fits(sheet.music, track, notationId)
    }
}

data class ClipEditorState(
    val clip: Clip? = null,
    val isClipStale: Boolean = false,
    val isPopoverOpen: Boolean = false,
    val clips: List<Clip> = emptyList(),
)
