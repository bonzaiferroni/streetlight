package koala.dom.component

import js.array.iterator
import kampfire.model.MutableTap
import kampfire.model.Url
import kampfire.model.toUrl
import koala.dom.ViewScope
import koala.dom.layout.flowBlock
import koala.modifier.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import web.blob.Blob
import web.blob.BlobPropertyBag
import web.events.EventType
import web.events.addEventListener
import web.mediadevices.getUserMedia
import web.mediarecorder.BlobEvent
import web.mediarecorder.DATA_AVAILABLE
import web.mediarecorder.MediaRecorder
import web.mediarecorder.MediaRecorderOptions
import web.mediastreams.MediaStream
import web.mediastreams.MediaStreamConstraints
import web.mediastreams.MediaTrackConstraints
import web.navigator.navigator
import web.url.URL

/** The bits per second a recording is made at, within what the server keeps without encoding again. */
private const val RECORD_BITS_PER_SECOND = 96_000

/**
 * An audio recorder bound to [field]: a button that records from the microphone until it is pressed again, and a
 * player for the recording with a button to clear it once set. [field] receives a `blob:` URL of the recording.
 */
fun ViewScope.recordAudio(
    field: MutableTap<Url?>,
    mod: Modifier? = null,
) = flowBlock(field, modify(mod)) { url ->
    if (url != null) {
        audioPlayer(url) { field.set(null) }
        return@flowBlock
    }

    val recorderState = MutableStateFlow<MediaRecorder?>(null)
    var stream: MediaStream? = null
    var disposed = false
    fun stopStream() {
        stream?.getTracks()?.let { for (track in it) track.stop() }
        stream = null
    }
    onDispose {
        disposed = true
        stopStream()
    }

    flowBlock(null, recorderState) { recorder ->
        if (recorder != null) {
            button("Stop", onClick = { recorder.stop() })
            return@flowBlock
        }
        button("Record", onClick = {
            contentScope.launch {
                val micStream = runCatching {
                    navigator.mediaDevices.getUserMedia(MediaStreamConstraints(audio = MediaTrackConstraints()))
                }.onFailure { console.log("microphone unavailable: $it") }.getOrNull() ?: return@launch
                stream = micStream

                val newRecorder = MediaRecorder(micStream, MediaRecorderOptions(audioBitsPerSecond = RECORD_BITS_PER_SECOND))
                val chunks = mutableListOf<Blob>()
                newRecorder.addEventListener(BlobEvent.DATA_AVAILABLE, { chunks.add(it.data) })
                newRecorder.addEventListener(EventType("stop"), {
                    stopStream()
                    if (disposed) return@addEventListener
                    recorderState.value = null
                    val blob = Blob(chunks.toTypedArray(), BlobPropertyBag(type = newRecorder.mimeType))
                    field.set(URL.createObjectURL(blob).toUrl())
                })
                newRecorder.start()
                recorderState.value = newRecorder
            }
        })
    }
}
