@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.model

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import kampfire.model.Tap
import kampfire.model.storeOf
import streetlight.model.music.MelodyTrack
import streetlight.model.music.MusicSheet
import streetlight.model.music.Track
import streetlight.model.music.TrackClip
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

/** Plays the tracks of a song through the speakers on [audio], its tones sounded by [synth]. */
class TrackPlayer(
    private val audio: StudioAudio,
    private val synth: ToneSynth,
) {
    private val sources = mutableListOf<AudioScheduledSourceNode>()
    private var bus: GainNode? = null
    private var startClock = 0.0

    private val playingStore = storeOf(false)
    val isPlayingState: Tap<Boolean> = playingStore

    /**
     * Plays [tracks] of [sheet], or every track when it is `null`, through the song's arrangement. Each section
     * lasts its measures, and a track rests through a section it does not play or runs short of. Playback begins at [fromSeconds] from the first downbeat, before it for a pickup to sound, or at the first
     * sound when it is `null`. Any playback already running is stopped first.
     *
     * Each of [musicWaves] plays in place of the tones of the track or notation its binding names.
     */
    fun play(
        sheet: MusicSheet,
        tracks: List<Track>? = null,
        fromSeconds: Double? = null,
        musicWaves: Map<TrackClip, MusicWave> = emptyMap(),
    ) {
        stop()
        val plan = songPlanOf(sheet, tracks ?: sheet.tracks, musicWaves) ?: return
        val context = audio.startContext()
        val runBus = context.createGain().also { it.connect(context.destination) }
        bus = runBus
        val startTick = fromSeconds?.let { (it / plan.secondsPerTick).toInt() } ?: plan.firstTick
        startClock = context.currentTime + START_DELAY_SECONDS - startTick * plan.secondsPerTick
        schedule(context, runBus, plan, startTick, startClock) { keep(it) }
        playingStore.set(sources.isNotEmpty())
    }

    /**
     * Renders every track of [sheet] from its first sound to the end of its last, with [musicWaves] in place of
     * tones as [play] would sound them, into mono PCM at [RENDER_RATE], or `null` when it sounds nothing.
     */
    suspend fun render(sheet: MusicSheet, musicWaves: Map<TrackClip, MusicWave> = emptyMap()): Wave? {
        val plan = songPlanOf(sheet, sheet.tracks, musicWaves) ?: return null
        val startClock = -plan.firstTick * plan.secondsPerTick
        val seconds = plan.endClockOf(startClock) + RENDER_TAIL_SECONDS
        val context = OfflineAudioContext(1, ceil(seconds * RENDER_RATE).toInt(), RENDER_RATE.toFloat())
        schedule(context, context.destination, plan, plan.firstTick, startClock) { }
        val buffer = context.startRendering()
        return Wave(RENDER_RATE, listOf(buffer.getChannelData(0).unsafeCast<FloatArray>()))
    }

    /** What [playing], tracks of [sheet], sound with [musicWaves] in place of tones, or `null` for nothing. */
    private fun songPlanOf(sheet: MusicSheet, playing: List<Track>, musicWaves: Map<TrackClip, MusicWave>): SongPlan? {
        val music = sheet.music
        val playingWaves = musicWaves.filterKeys { trackClip -> playing.any { it.trackId == trackClip.trackId } }
        val trackTones = songTonesOf(music, playing, playingWaves.keys).filter { it.isSounding }
        // each time a wave's track plays its notation, placed by the notation's tick, and a whole track's at the start
        val waveStarts = playingWaves.flatMap { (trackClip, musicWave) ->
            val waveTrack = playing.first { it.trackId == trackClip.trackId }
            val starts = trackClip.notationId?.let { notationStartsOf(music, waveTrack, it) } ?: listOf(0)
            starts.map { it to musicWave }
        }
        if (trackTones.isEmpty() && waveStarts.isEmpty()) return null
        return SongPlan(trackTones, waveStarts, music.secondsPerTick)
    }

    /**
     * Schedules [plan] into [bus] of [context] from [startTick], tick 0 falling at [startClock], handing each source it
     * starts to [keep].
     */
    private fun schedule(
        context: BaseAudioContext,
        bus: AudioNode,
        plan: SongPlan,
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
        plan.trackTones.forEach { (track, tones, drums) ->
            drums.filter { it.startTick >= startTick }.forEach { drum ->
                val start = startClock + drum.startTick * secondsPerTick
                synth.strike(context, bus, drum.drum, drum.hit, start).forEach(keep)
            }
            val level = if (track is MelodyTrack) ToneSynth.MELODY_LEVEL else ToneSynth.ACCOMPANIMENT_LEVEL
            // a tone already sounding where playback begins sounds what is left of it
            tones.filter { it.startTick + it.ticks > startTick }.forEach { tone ->
                val toneStart = max(tone.startTick, startTick)
                val start = startClock + toneStart * secondsPerTick
                val duration = (tone.startTick + tone.ticks - toneStart) * secondsPerTick
                val voiceLevel = level / sqrt(tone.midi.size.toDouble())
                tone.midi.forEach { midi -> synth.voice(context, bus, midi, start, duration, voiceLevel).forEach(keep) }
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

/** The [trackTones] and [waveStarts] a song sounds, each wave by the tick it starts at, at [secondsPerTick]. */
private class SongPlan(
    val trackTones: List<TrackTones>,
    val waveStarts: List<Pair<Int, MusicWave>>,
    val secondsPerTick: Double,
) {
    /** The tick of the first sound: a pickup's or a wave's lead may sound before the first downbeat. */
    val firstTick = (trackTones.mapNotNull { it.firstTick } +
        waveStarts.map { (tick, musicWave) -> tick - (musicWave.leadSeconds / secondsPerTick).toInt() }).min()

    /** Where the last sound ends on the clock, with tick 0 at [startClock]. */
    fun endClockOf(startClock: Double): Double {
        val toneEnd = trackTones
            .flatMap { tones -> tones.tones.map { it.startTick + it.ticks } + tones.drums.map { it.startTick } }
            .maxOrNull()?.let { startClock + it * secondsPerTick } ?: startClock
        val waveEnd = waveStarts.maxOfOrNull { (tick, musicWave) ->
            startClock + tick * secondsPerTick - musicWave.leadSeconds + musicWave.wave.durationSeconds
        } ?: startClock
        return max(toneEnd, waveEnd)
    }
}
