@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.model

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import kampfire.model.Tap
import kampfire.model.storeOf
import web.audio.AudioBuffer
import web.audio.AudioBufferSourceNode
import web.audio.AudioContext
import web.events.EventHandler

/** Plays PCM samples together through the speakers. */
class AudioPlayer(
    private val audio: StudioAudio,
) {
    private val sources = mutableListOf<AudioBufferSourceNode>()

    private val playingStore = storeOf(false)
    val isPlayingState: Tap<Boolean> = playingStore

    /**
     * Plays [placements] together from [fromSeconds] on the timeline, and returns the [StudioAudio.clockSeconds]
     * playback began at. A placement that begins later waits for its start. Any playback already running is stopped
     * first.
     */
    fun play(placements: List<PlacedPcm>, fromSeconds: Double): Double {
        stop()
        val context = audio.readContext()
        val startClock = context.currentTime

        placements.forEach { placement ->
            val offset = fromSeconds - placement.startSeconds
            if (offset >= placement.pcm.durationSeconds) return@forEach
            val source = context.createBufferSource()
            source.buffer = toAudioBuffer(context, placement.pcm)
            source.connect(context.destination)
            source.onended = EventHandler { endSource(source) }
            if (offset >= 0) source.start(startClock, offset) else source.start(startClock - offset, 0.0)
            sources.add(source)
        }

        playingStore.set(sources.isNotEmpty())
        return startClock
    }

    /** Stops every sample playing. */
    fun stop() {
        val current = sources.toList()
        sources.clear()
        current.forEach { source ->
            source.onended = null
            source.stop()
        }
        playingStore.set(false)
    }

    private fun endSource(source: AudioBufferSourceNode) {
        sources.remove(source)
        if (sources.isEmpty()) playingStore.set(false)
    }

    private fun toAudioBuffer(context: AudioContext, sample: PcmSample): AudioBuffer {
        val buffer = context.createBuffer(sample.channels.size, sample.frameCount, sample.sampleRate.toFloat())
        sample.channels.forEachIndexed { index, samples ->
            buffer.copyToChannel(samples.unsafeCast<Float32Array<ArrayBuffer>>(), index)
        }
        return buffer
    }
}

/** PCM placed on the timeline at [startSeconds]. */
class PlacedPcm(
    val pcm: PcmSample,
    val startSeconds: Double,
)
