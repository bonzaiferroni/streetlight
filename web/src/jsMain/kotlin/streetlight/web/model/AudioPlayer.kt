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
     * Plays [samples] together, each from [fromSeconds] into it, and returns the [StudioAudio.clockSeconds]
     * playback began at. Any playback already running is stopped first.
     */
    fun play(samples: List<PcmSample>, fromSeconds: Double): Double {
        stop()
        val context = audio.readContext()
        val startClock = context.currentTime

        for (sample in samples) {
            if (sample.durationSeconds <= fromSeconds) continue
            val source = context.createBufferSource()
            source.buffer = toAudioBuffer(context, sample)
            source.connect(context.destination)
            source.onended = EventHandler { endSource(source) }
            source.start(startClock, fromSeconds)
            sources.add(source)
        }

        playingStore.set(sources.isNotEmpty())
        return startClock
    }

    /** Stops every sample playing. */
    fun stop() {
        val current = sources.toList()
        sources.clear()
        for (source in current) {
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
