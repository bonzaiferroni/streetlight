package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.combine
import kampfire.model.tapOf
import streetlight.model.music.Clip
import streetlight.model.music.ClipId
import streetlight.model.music.Mix
import streetlight.model.music.TrackSectionId

/**
 * Edits the clip bound to the track's section [trackSectionId] in the mix [editor] has open, choosing among the clips
 * of the library that fit the section, or none.
 */
class SectionClipEditor(
    private val editor: MixEditor,
    private val trackSectionId: TrackSectionId,
) {
    private val boundClipIdState = editor.boundClipIdOf(trackSectionId)

    /** The clips to choose from: those that fit the section, and the bound one whether it fits or not. */
    private val choicesState = editor.clipsState.combine(editor.mixState) { clips, mix -> clips to mix }
        .combine(boundClipIdState) { (clips, mix), boundId -> choicesOf(clips, mix, boundId) }

    /** The name of each choice, after the one for none. */
    val namesState: Tap<List<String>> = choicesState.tapOf { choices -> listOf(NO_CLIP) + choices.map { it.name } }

    /** The name of the bound clip, or the one for none; setting it binds the clip of that name. */
    val selectedNameState: MutableTap<String> = object : MutableTap<String> {
        private val tap = choicesState.combine(boundClipIdState) { choices, boundId ->
            choices.firstOrNull { it.clipId == boundId }?.name ?: NO_CLIP
        }
        override val flow get() = tap.flow
        override val now get() = tap.now
        override fun set(value: String) = select(value)
        override fun set(setter: String.() -> String) = select(now.setter())
        override fun update(transform: (String) -> String) = select(transform(now))
    }

    private fun select(name: String) {
        if (name == NO_CLIP) return editor.removeClip(trackSectionId)
        val clip = choicesState.now.firstOrNull { it.name == name } ?: return
        editor.putClip(trackSectionId, clip.clipId)
    }

    private fun choicesOf(clips: List<Clip>, mix: Mix?, boundId: ClipId?): List<Clip> {
        mix ?: return emptyList()
        val track = editor.musicEditor.readTrack(trackSectionId.trackId) ?: return emptyList()
        val music = editor.musicEditor.musicState.now
        return clips.filter { it.clipId == boundId || it.fits(music, mix, track, trackSectionId.sectionId) }.sortedBy { it.name }
    }
}

private const val NO_CLIP = "(none)"
