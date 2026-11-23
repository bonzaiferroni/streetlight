@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.model

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import kampfire.model.Tap
import kampfire.model.storeOf
import web.audio.AudioNode
import web.audio.BaseAudioContext
import web.audio.OfflineAudioContext
import web.audio.startRendering
import web.audio.AudioScheduledSourceNode
import web.audio.GainNode
import web.events.EventHandler
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

/** Plays a [MusicPlan] through the speakers on [audio], its tones sounded by [synth]. */
class MusicPlayer(
    private val audio: StudioAudio,
    private val synth: ToneSynth,
) {
    private val sources = mutableListOf<AudioScheduledSourceNode>()
    private var bus: GainNode? = null
    private var startClock = 0.0

    private val playingStore = storeOf(false)
    val isPlayingState: Tap<Boolean> = playingStore

    /**
     * Plays [plan] from [fromSeconds] after its first downbeat, before it for a pickup to sound, or from its first sound
     * when it is `null`. Any playback already running is stopped first.
     */
    fun play(plan: MusicPlan, fromSeconds: Double? = null) {
        stop()
        val context = audio.startContext()
        val runBus = context.createGain().also { it.connect(context.destination) }
        bus = runBus
        val startTick = fromSeconds?.let { (it / plan.secondsPerTick).toInt() } ?: plan.firstTick
        startClock = context.currentTime + START_DELAY_SECONDS - startTick * plan.secondsPerTick
        schedule(context, runBus, plan, startTick, startClock) { keep(it) }
        playingStore.set(sources.isNotEmpty())
    }

    /** Renders [plan] from its first sound to the end of its last into mono PCM at [RENDER_RATE]. */
    suspend fun render(plan: MusicPlan): Wave {
        val startClock = -plan.firstTick * plan.secondsPerTick
        val seconds = plan.endClockOf(startClock) + RENDER_TAIL_SECONDS
        val context = OfflineAudioContext(1, ceil(seconds * RENDER_RATE).toInt(), RENDER_RATE.toFloat())
        schedule(context, context.destination, plan, plan.firstTick, startClock) { }
        val buffer = context.startRendering()
        return Wave(RENDER_RATE, listOf(buffer.getChannelData(0).unsafeCast<FloatArray>()))
    }

    /**
     * Schedules [plan] into [bus] of [context] from [startTick], tick 0 falling at [startClock], handing each source it
     * starts to [keep].
     */
    private fun schedule(
        context: BaseAudioContext,
        bus: AudioNode,
        plan: MusicPlan,
        startTick: Int,
        startClock: Double,
        keep: (AudioScheduledSourceNode) -> Unit,
    ) {
        val secondsPerTick = plan.secondsPerTick
        val playClock = startClock + startTick * secondsPerTick
        plan.waveStarts.forEach { (tick, musicWave) ->
            val at = startClock + tick * secondsPerTick - musicWave.leadSeconds
            // a wave already under way where playback begins plays what is left of it
            val offset = max(playClock - at, 0.0)
            if (offset < musicWave.wave.durationSeconds) keep(waveSource(context, bus, musicWave.wave, at + offset, offset))
        }
        plan.layers.flatMap { it.tones }.forEach { tone ->
            when (tone) {
                is DrumTone -> if (tone.startTick >= startTick) {
                    val start = startClock + tone.startTick * secondsPerTick
                    synth.strike(context, bus, tone.drum, tone.volume, start).forEach(keep)
                }
                // a note already sounding where playback begins sounds what is left of it
                is NoteTone -> if (tone.endTick > startTick) {
                    val toneStart = max(tone.startTick, startTick)
                    val start = startClock + toneStart * secondsPerTick
                    val duration = (tone.endTick - toneStart) * secondsPerTick
                    val voiceLevel = ToneSynth.VOICE_LEVEL * tone.volume / sqrt(tone.midi.size.toDouble())
                    tone.midi.forEach { midi -> synth.voice(context, bus, midi, start, duration, voiceLevel).forEach(keep) }
                }
            }
        }
    }

    /** The seconds from the first downbeat the latest playback has reached, below 0 before it. */
    fun readSeconds() = audio.clockSeconds - startClock

    /** Stops every tone playing, silencing their release tails at once. */
    fun stop() {
        bus?.disconnect()
        bus = null
        val current = sources.toList()
        sources.clear()
        current.forEach { source ->
            source.onended = null
            source.stop()
        }
        playingStore.set(false)
    }

    /** Plays [wave] into [bus] from [start] on the audio clock, [offset] seconds into it. */
    private fun waveSource(
        context: BaseAudioContext,
        bus: AudioNode,
        wave: Wave,
        start: Double,
        offset: Double,
    ): AudioScheduledSourceNode {
        val buffer = context.createBuffer(wave.channels.size, wave.frameCount, wave.sampleRate.toFloat())
        wave.channels.forEachIndexed { index, samples ->
            buffer.copyToChannel(samples.unsafeCast<Float32Array<ArrayBuffer>>(), index)
        }
        val source = context.createBufferSource()
        source.buffer = buffer
        source.connect(bus)
        source.start(start, offset)
        return source
    }

    /** Keeps [source] until it ends, so playback is known to run while any source does. */
    private fun keep(source: AudioScheduledSourceNode) {
        source.onended = EventHandler { endSource(source) }
        sources.add(source)
    }

    private fun endSource(source: AudioScheduledSourceNode) {
        sources.remove(source)
        if (sources.isEmpty()) playingStore.set(false)
    }
}

private const val START_DELAY_SECONDS = 0.1
private const val RENDER_RATE = 48_000
private const val RENDER_TAIL_SECONDS = 1.0
