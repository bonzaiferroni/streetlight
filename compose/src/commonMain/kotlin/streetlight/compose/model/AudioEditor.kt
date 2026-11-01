package streetlight.compose.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import streetlight.model.data.Audio
import streetlight.model.io.ApiClient

/**
 * The audio of an edit: the stored one, or a file picked from the device and held until the edit is saved, when it is
 * uploaded and encoded. A submodel, held by the editor of an audio post.
 */
class AudioEditor(private val api: ApiClient) {
    private val store = storeOf(AudioEditorState())

    val audioState = store.tapOf { it.audio }
    val pickedState = store.tapOf { it.picked }

    /** Starts from the stored [audio], if any, with nothing picked. */
    fun setAudio(audio: Audio?) = store.set { AudioEditorState(audio) }

    /** Holds the audio of [bytes], picked from the file [name], in place of the stored one. */
    fun pick(name: String, bytes: ByteArray) = store.set { copy(picked = PickedAudio(name, bytes)) }

    /** Leaves the edit with no audio. */
    fun clear() = store.set { AudioEditorState() }

    /** Uploads picked audio and keeps the stored audio it becomes; the audio as it stands when nothing was picked. */
    suspend fun finalizeAudio(): Outcome<Audio?> {
        val picked = store.now.picked ?: return Ok(store.now.audio)
        val audio = api.user.uploadAudio(picked.name, picked.bytes).toDataOr { return it }
        store.set { AudioEditorState(audio) }
        return Ok(audio)
    }
}

/** The stored [audio] of an [AudioEditor], and the file [picked] to take its place. */
data class AudioEditorState(
    val audio: Audio? = null,
    val picked: PickedAudio? = null,
)

/** An audio file picked from the device, its [name] and [bytes], not yet uploaded. */
class PickedAudio(val name: String, val bytes: ByteArray)
