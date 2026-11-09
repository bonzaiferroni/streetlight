package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.combine
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.music.Clip
import streetlight.model.music.Mix
import streetlight.model.music.SectionClip
import streetlight.model.music.SectionId
import streetlight.model.music.TrackId

/**
 * Edits the clip bound to the section [sectionId] of the track [trackId] in the mix [editor] has open, choosing among
 * the clips of [clipLibrary] that fit the section, or none. The clips are read again whenever the mix changes, so a new
 * take joins them.
 */
class SectionClipEditor(
    scope: CoroutineScope,
    private val editor: MixEditor,
    private val trackId: TrackId,
    private val sectionId: SectionId,
    private val clipLibrary: ClipLibrary,
) {
    private val clipsStore = storeOf<List<Clip>>(emptyList())

    /** The clips to choose from: those that fit the section, and the bound one whether it fits or not. */
    private val choicesState = clipsStore.combine(editor.mixState) { clips, mix -> choicesOf(clips, mix) }

    /** The name of each choice, after the one for none. */
    val namesState: Tap<List<String>> = choicesState.tapOf { choices -> listOf(NO_CLIP) + choices.map { it.name } }

    /** The name of the bound clip, or the one for none; setting it binds the clip of that name. */
    val selectedNameState: MutableTap<String> = object : MutableTap<String> {
        private val tap = choicesState.combine(editor.mixState) { choices, mix ->
            val clipId = mix?.boundClipId()
            choices.firstOrNull { it.clipId == clipId }?.name ?: NO_CLIP
        }
        override val flow get() = tap.flow
        override val now get() = tap.now
        override fun set(value: String) = select(value)
        override fun set(setter: String.() -> String) = select(now.setter())
        override fun update(transform: (String) -> String) = select(transform(now))
    }

    init {
        scope.launch { editor.mixState.flow.collect { clipsStore.set(clipLibrary.readClips()) } }
    }

    private fun select(name: String) {
        if (name == NO_CLIP) return editor.removeClip(trackId, sectionId)
        val clip = choicesState.now.firstOrNull { it.name == name } ?: return
        editor.putClip(SectionClip(trackId, sectionId, clip.clipId))
    }

    private fun choicesOf(clips: List<Clip>, mix: Mix?): List<Clip> {
        mix ?: return emptyList()
        val track = editor.musicEditor.readTrack(trackId) ?: return emptyList()
        val music = editor.musicEditor.musicState.now
        val boundId = mix.boundClipId()
        return clips.filter { it.clipId == boundId || it.fits(music, mix, track, sectionId) }.sortedBy { it.name }
    }

    private fun Mix.boundClipId() = clips.firstOrNull { it.trackId == trackId && it.sectionId == sectionId }?.clipId
}

private const val NO_CLIP = "(none)"
