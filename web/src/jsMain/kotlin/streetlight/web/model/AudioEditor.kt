package streetlight.web.model

import js.typedarrays.Int8Array
import kampfire.model.Messenger
import kampfire.model.MutableTap
import kampfire.model.PrintLnMessenger
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import kampfire.model.mutableTapOf
import kampfire.model.toDataOr
import streetlight.model.data.MediaEdit
import streetlight.model.data.MediaType
import streetlight.model.io.ApiClient
import web.http.arrayBuffer
import web.http.fetch

/** Uploads the audio of the edit in [editField] when it is still a local blob, keeping the stored audio as its meta. */
class AudioEditor(
    private val editField: MutableTap<MediaEdit>,
    private val api: ApiClient,
) {
    /** The url of the edit's audio: a local blob once picked, the stored file once uploaded. */
    val urlField = editField.mutableTapOf({ it.url }) { copy(url = it, meta = null) }

    /** Uploads a blob audio file of an audio post and keeps the stored audio. Returns `false` when the upload fails. */
    suspend fun finalizeAudio(messenger: Messenger? = null): Boolean {
        val edit = editField.now
        if (edit.mediaType != MediaType.Audio) return true
        val blobUrl = edit.url?.takeIf { it.isBlob } ?: return true
        messenger?.deliver(UIMessage("Uploading audio...", UIMessageType.Working))
        val buffer = fetch(blobUrl.value).arrayBuffer()
        val audio = api.user.uploadAudio(edit.title, Int8Array(buffer).unsafeCast<ByteArray>())
            .toDataOr(messenger ?: PrintLnMessenger, "Audio uploaded.") { return false }
        editField.set(editField.now.copy(url = audio.url, meta = audio))
        return true
    }
}
