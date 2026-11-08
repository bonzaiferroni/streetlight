@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.model

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import kampfire.model.Tap
import kampfire.model.storeOf
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Track
import web.audio.AudioNode
import web.audio.BaseAudioContext
import web.audio.OfflineAudioContext
import web.audio.startRendering
import web.audio.AudioScheduledSourceNode
import web.audio.GainNode
import web.events.EventHandler
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/** Plays the tracks of a song through the speakers, each note a few harmonics shaped by an envelope. */
class TrackPlayer(
    private val audio: StudioAudio,
) {
    private val kit = DrumKit()
    private val sources = mutableListOf<AudioScheduledSourceNode>()
    private var bus: GainNode? = null
    private var startClock = 0.0
    private var noteBus: GainNode? = null

    private val playingStore = storeOf(false)
    val isPlayingState: Tap<Boolean> = playingStore

    /**
     * Plays [tracks] of [song], or every track when it is `null`, through the song's arrangement. Each section
     * lasts as long as the longest track that defines it, and a track rests through a section it lacks or runs short
     * of. Playback begins at [fromSeconds] from the first downbeat, before it for a pickup to sound, or at the first
     * sound when it is `null`. Any playback already running is stopped first.
     *
     * Each of [takes] plays in place of the tones of its section, and the song then runs at the takes' tempo.
     */
    fun play(song: StoredSong, tracks: List<Track>? = null, fromSeconds: Double? = null, takes: SongTakes? = null) {
        stop()
        val plan = songPlanOf(song, tracks ?: song.tracks, takes) ?: return
        val context = audio.readContext()
        val runBus = context.createGain().also { it.connect(context.destination) }
        bus = runBus
        val startTick = fromSeconds?.let { (it / plan.secondsPerTick).toInt() } ?: plan.firstTick
        startClock = context.currentTime + START_DELAY_SECONDS - startTick * plan.secondsPerTick
        schedule(context, runBus, plan, startTick, startClock) { keep(it) }
        playingStore.set(sources.isNotEmpty())
    }

    /**
     * Renders every track of [song] from its first sound to the end of its last, with [takes] in place of their
     * sections' tones as [play] would sound them, into mono PCM at [RENDER_RATE], or `null` when it sounds nothing.
     */
    suspend fun render(song: StoredSong, takes: SongTakes? = null): PcmSample? {
        val plan = songPlanOf(song, song.tracks, takes) ?: return null
        val startClock = -plan.firstTick * plan.secondsPerTick
        val seconds = plan.endClockOf(startClock) + RENDER_TAIL_SECONDS
        val context = OfflineAudioContext(1, ceil(seconds * RENDER_RATE).toInt(), RENDER_RATE.toFloat())
        schedule(context, context.destination, plan, plan.firstTick, startClock) { }
        val buffer = context.startRendering()
        return PcmSample(RENDER_RATE, listOf(buffer.getChannelData(0).unsafeCast<FloatArray>()))
    }

    /** What [playing], tracks of [song], sound with [takes] in place of their sections' tones, or `null` for nothing. */
    private fun songPlanOf(song: StoredSong, playing: List<Track>, takes: SongTakes?): SongPlan? {
        val music = song.music
        val playingTakes = takes?.takes.orEmpty().filterKeys { key -> playing.any { it.trackId == key.trackId } }
        val trackTones = songTonesOf(music, song.tracks, playing, playingTakes.keys).filter { it.isSounding }
        // each time a take's track plays its section, placed by the section's tick
        val takeStarts = playingTakes.flatMap { (key, take) ->
            val takeTrack = playing.first { it.trackId == key.trackId }
            sectionStartsOf(music, song.tracks, takeTrack, key.sectionId).map { it to take }
        }
        if (trackTones.isEmpty() && takeStarts.isEmpty()) return null
        val secondsPerTick = takes?.let { 60.0 / it.bpm / music.time.pulse.ticks } ?: music.secondsPerTick
        return SongPlan(trackTones, takeStarts, secondsPerTick)
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
        plan.takeStarts.forEach { (tick, take) ->
            val at = startClock + tick * secondsPerTick - take.leadSeconds
            // a take already under way where playback begins plays what is left of it
            val offset = max(playClock - at, 0.0)
            if (offset < take.pcm.durationSeconds) keep(pcmSource(context, bus, take.pcm, at + offset, offset))
        }
        plan.trackTones.forEach { (track, tones, drums) ->
            drums.filter { it.startTick >= startTick }.forEach { drum ->
                val start = startClock + drum.startTick * secondsPerTick
                kit.strike(context, bus, drum.drum, drum.hit, start).forEach(keep)
            }
            val level = if (track is MelodyTrack) MELODY_LEVEL else ACCOMPANIMENT_LEVEL
            // a tone already sounding where playback begins sounds what is left of it
            tones.filter { it.startTick + it.ticks > startTick }.forEach { tone ->
                val toneStart = max(tone.startTick, startTick)
                val start = startClock + toneStart * secondsPerTick
                val duration = (tone.startTick + tone.ticks - toneStart) * secondsPerTick
                val voiceLevel = level / sqrt(tone.midi.size.toDouble())
                tone.midi.forEach { midi -> voice(context, bus, midi, start, duration, voiceLevel).forEach(keep) }
            }
        }
    }

    /**
     * Sounds [midis] together over any playback for [seconds] from [startClock] on the audio clock, or briefly from
     * now, cutting off what this last sounded.
     */
    fun playNotes(midis: List<Int>, startClock: Double? = null, seconds: Double = NOTE_SECONDS) {
        val context = audio.readContext()
        noteBus?.disconnect()
        val runBus = context.createGain().also { it.connect(context.destination) }
        noteBus = runBus
        val level = MELODY_LEVEL / sqrt(midis.size.toDouble())
        midis.forEach { voice(context, runBus, it, startClock ?: context.currentTime, seconds, level).forEach(::keep) }
    }

    /** Silences what [playNotes] last sounded. */
    fun stopNotes() {
        noteBus?.disconnect()
        noteBus = null
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

    /**
     * Sounds [midi] into [bus] from [start] for [duration] seconds at [level]: its first harmonics under one envelope
     * that blooms, relaxes into a softer sustain, and releases just before the note ends. Returns the oscillators.
     */
    private fun voice(
        context: BaseAudioContext,
        bus: AudioNode,
        midi: Int,
        start: Double,
        duration: Double,
        level: Double,
    ): List<AudioScheduledSourceNode> {
        val envelope = context.createGain()
        envelope.gain.setValueAtTime(0f, start)
        envelope.gain.linearRampToValueAtTime(level.toFloat(), start + ATTACK_SECONDS)
        envelope.gain.setTargetAtTime((level * SUSTAIN_RATIO).toFloat(), start + SETTLE_SECONDS, SETTLE_TIME_CONSTANT)
        envelope.gain.setTargetAtTime(0f, start + duration * RELEASE_POINT, RELEASE_TIME_CONSTANT)
        envelope.connect(bus)

        val frequency = 440.0 * 2.0.pow((midi - 69) / 12.0)
        return HARMONICS.map { (multiple, amplitude) ->
            val oscillator = context.createOscillator()
            oscillator.frequency.value = (frequency * multiple).toFloat()
            val gain = context.createGain()
            gain.gain.value = amplitude.toFloat()
            oscillator.connect(gain)
            gain.connect(envelope)
            oscillator.start(start)
            // the release tail outlasts the note, so the oscillators run on until it has faded
            oscillator.stop(start + duration + TAIL_SECONDS)
            oscillator
        }
    }

    /** Plays [pcm] into [bus] from [start] on the audio clock, [offset] seconds into it. */
    private fun pcmSource(
        context: BaseAudioContext,
        bus: AudioNode,
        pcm: PcmSample,
        start: Double,
        offset: Double,
    ): AudioScheduledSourceNode {
        val buffer = context.createBuffer(pcm.channels.size, pcm.frameCount, pcm.sampleRate.toFloat())
        pcm.channels.forEachIndexed { index, samples ->
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

/** The [trackTones] and [takeStarts] a song sounds, each take by the tick of its section, at [secondsPerTick]. */
private class SongPlan(
    val trackTones: List<TrackTones>,
    val takeStarts: List<Pair<Int, Take>>,
    val secondsPerTick: Double,
) {
    /** The tick of the first sound: a pickup's or a take's lead may sound before the first downbeat. */
    val firstTick = (trackTones.mapNotNull { it.firstTick } +
        takeStarts.map { (tick, take) -> tick - (take.leadSeconds / secondsPerTick).toInt() }).min()

    /** Where the last sound ends on the clock, with tick 0 at [startClock]. */
    fun endClockOf(startClock: Double): Double {
        val toneEnd = trackTones
            .flatMap { tones -> tones.tones.map { it.startTick + it.ticks } + tones.drums.map { it.startTick } }
            .maxOrNull()?.let { startClock + it * secondsPerTick } ?: startClock
        val takeEnd = takeStarts.maxOfOrNull { (tick, take) ->
            startClock + tick * secondsPerTick - take.leadSeconds + take.pcm.durationSeconds
        } ?: startClock
        return max(toneEnd, takeEnd)
    }
}
private const val NOTE_SECONDS = 0.35
private const val MELODY_LEVEL = 0.18
private const val ACCOMPANIMENT_LEVEL = 0.12

/** The multiple of the fundamental and the relative level of each harmonic a voice sounds. */
private val HARMONICS = listOf(1 to 1.0, 2 to 0.3, 3 to 0.12)

private const val ATTACK_SECONDS = 0.03
private const val SETTLE_SECONDS = 0.05
private const val SETTLE_TIME_CONSTANT = 0.2f
private const val SUSTAIN_RATIO = 0.65

/** The point in a note, as a share of its length, where its release begins. */
private const val RELEASE_POINT = 0.92
private const val RELEASE_TIME_CONSTANT = 0.05f
private const val TAIL_SECONDS = 0.3
