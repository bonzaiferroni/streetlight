@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.storeOf
import kotlinx.coroutines.CompletableDeferred
import web.audio.AudioContext
import web.audio.AudioWorkletNode
import web.audio.AudioWorkletNodeOptions
import web.audio.AudioWorkletProcessorName
import web.blob.Blob
import web.blob.BlobPropertyBag
import web.events.EventHandler
import web.mediadevices.getUserMedia
import web.mediastreams.MediaStream
import web.mediastreams.MediaStreamTrack
import kampfire.model.Labeled
import web.mediastreams.MediaStreamConstraints
import web.mediastreams.MediaTrackConstraints
import web.navigator.navigator
import web.url.URL
import web.worklets.addModule

/** Records PCM from the microphone on the context [startContext] starts, one capture at a time. */
class AudioRecorder(
    private val startContext: () -> AudioContext,
) {
    private var capture: AudioCapture? = null
    private var isStarting = false
    private var isModuleLoaded = false

    private val recordingStore = storeOf(false)
    val isRecordingState: Tap<Boolean> = recordingStore

    /** The peaks of the capture in progress, or `null` when none is recording. */
    val livePeaks get() = capture?.peaks

    /** Starts a capture from the microphone, unless one is already recording. */
    suspend fun start() {
        if (capture != null || isStarting) return
        isStarting = true
        try {
            val context = startContext()
            loadModule(context)
            val stream = navigator.mediaDevices.getUserMedia(MediaStreamConstraints(audio = MediaTrackConstraints(
                echoCancellation = false,
                noiseSuppression = false,
                autoGainControl = false,
            )))
            val source = context.createMediaStreamSource(stream)
            val node = AudioWorkletNode(context, AudioWorkletProcessorName(TAP_PROCESSOR), AudioWorkletNodeOptions(numberOfOutputs = 0))
            source.connect(node)
            capture = AudioCapture(context.sampleRate.toInt(), stream, node)
            recordingStore.set(true)
        } finally {
            isStarting = false
        }
    }

    /** Stops the capture, and returns what it recorded, or `null` when none is recording. */
    suspend fun stop(): CapturedPcm? {
        val current = capture ?: return null
        capture = null
        recordingStore.set(false)
        return current.stop()
    }

    /** Stops the capture and discards what it recorded. */
    fun cancel() {
        val current = capture ?: return
        capture = null
        recordingStore.set(false)
        current.release()
    }

    private suspend fun loadModule(context: AudioContext) {
        if (isModuleLoaded) return
        val moduleUrl = URL.createObjectURL(Blob(arrayOf(TAP_PROCESSOR_SOURCE), BlobPropertyBag(type = "text/javascript")))
        try {
            context.audioWorklet.addModule(moduleUrl)
        } finally {
            URL.revokeObjectURL(moduleUrl)
        }
        isModuleLoaded = true
    }
}

/** A recording in progress, which becomes a [CapturedPcm] once stopped. */
private class AudioCapture(
    private val sampleRate: Int,
    private val stream: MediaStream,
    private val node: AudioWorkletNode,
) {
    private val blocks = mutableListOf<Array<FloatArray>>()
    val peaks = BlockPeaks(sampleRate)
    private val ended = CompletableDeferred<Unit>()
    private var startClock = 0.0

    init {
        node.port.onmessage = EventHandler { event ->
            val data = event.data
            when {
                data == END_MESSAGE -> ended.complete(Unit)
                jsTypeOf(data) == "number" -> startClock = data.unsafeCast<Double>()
                else -> {
                    val block = data.unsafeCast<Array<FloatArray>>()
                    blocks.add(block)
                    peaks.add(block)
                }
            }
        }
    }

    /** Stops recording, and returns every sample captured since the capture began. */
    suspend fun stop(): CapturedPcm {
        node.port.postMessage(STOP_MESSAGE)
        try {
            ended.await()
        } finally {
            release()
        }
        val track = stream.getAudioTracks().first()
        return CapturedPcm(PcmSample(sampleRate, joinBlocks()), startClock, track.label, readProcessing(track))
    }

    /** Ends the capture without waiting for its last blocks, releasing the microphone. */
    fun release() {
        node.disconnect()
        node.port.close()
        stream.getTracks().forEach { it.stop() }
    }

    private fun joinBlocks(): List<FloatArray> {
        val channelCount = blocks.firstOrNull()?.size ?: return emptyList()
        val frameCount = blocks.sumOf { it[0].size }
        return List(channelCount) { channel ->
            val samples = FloatArray(frameCount)
            var offset = 0
            blocks.forEach { block ->
                val part = block[channel]
                part.copyInto(samples, offset)
                offset += part.size
            }
            samples
        }
    }
}

/**
 * What a capture recorded, beginning at [startClock] on the [StudioAudio] clock, from the mic named [micLabel] with
 * the browser's [processing] applied.
 */
class CapturedPcm(
    val pcm: PcmSample,
    val startClock: Double,
    val micLabel: String,
    val processing: List<BrowserProcessing>,
)

/** Processing the browser may apply to the mic, which the studio asks it not to. */
enum class BrowserProcessing(override val label: String): Labeled {
    EchoCancellation("echo cancellation"),
    NoiseSuppression("noise suppression"),
    AutoGainControl("auto gain"),
}

/** The processing [track] runs with, despite the studio's constraints. */
private fun readProcessing(track: MediaStreamTrack): List<BrowserProcessing> {
    val settings = track.getSettings()
    return buildList {
        if (settings.echoCancellation?.toString().let { it != null && it != "false" }) add(BrowserProcessing.EchoCancellation)
        if (settings.noiseSuppression == true) add(BrowserProcessing.NoiseSuppression)
        if (settings.autoGainControl == true) add(BrowserProcessing.AutoGainControl)
    }
}

private const val TAP_PROCESSOR = "pcm-tap"
private const val STOP_MESSAGE = "stop"
private const val END_MESSAGE = "end"

// posts the clock time of the first block of input, then a copy of each block; "end" follows the last block once
// "stop" arrives
private const val TAP_PROCESSOR_SOURCE = """
class PcmTapProcessor extends AudioWorkletProcessor {
    constructor() {
        super();
        this.stopped = false;
        this.started = false;
        this.port.onmessage = (event) => {
            if (event.data === "$STOP_MESSAGE") {
                this.stopped = true;
                this.port.postMessage("$END_MESSAGE");
            }
        };
    }

    process(inputs) {
        if (this.stopped) return false;
        const input = inputs[0];
        if (input.length === 0) return true;
        if (!this.started) {
            this.started = true;
            this.port.postMessage(currentTime);
        }
        this.port.postMessage(input.map((channel) => channel.slice()));
        return true;
    }
}
registerProcessor("$TAP_PROCESSOR", PcmTapProcessor);
"""
