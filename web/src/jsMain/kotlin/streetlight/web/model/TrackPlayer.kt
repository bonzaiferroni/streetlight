@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.model

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import kampfire.model.Tap
import kampfire.model.storeOf
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Track
import web.audio.AudioContext
import web.audio.AudioScheduledSourceNode
import web.audio.GainNode
import web.events.EventHandler
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
     * Plays [track] of [song] alone, or every track when it is `null`, through the song's arrangement. Each section
     * lasts as long as the longest track that defines it, and a track rests through a section it lacks or runs short
     * of. Playback begins at [fromSeconds] from the first downbeat, before it for a pickup to sound, or at the first
     * sound when it is `null`. Any playback already running is stopped first.
     *
     * Each of [takes] plays in place of the tones of its section, and the song then runs at the takes' tempo.
     */
    fun play(song: StoredSong, track: Track? = null, fromSeconds: Double? = null, takes: SongTakes? = null) {
        stop()
        val music = song.music
        val playing = track?.let { listOf(it) } ?: song.tracks
        val playingTakes = takes?.takes.orEmpty().filterKeys { key -> playing.any { it.trackId == key.trackId } }
        val trackTones = songTonesOf(music, song.tracks, playing, playingTakes.keys).filter { it.isSounding }
        // each time a take's track plays its section, placed by the section's tick
        val takeStarts = playingTakes.flatMap { (key, take) ->
            val takeTrack = playing.first { it.trackId == key.trackId }
            sectionStartsOf(music, song.tracks, takeTrack, key.sectionId).map { it to take }
        }
        if (trackTones.isEmpty() && takeStarts.isEmpty()) return

        val context = audio.readContext()
        val runBus = context.createGain().also { it.connect(context.destination) }
        bus = runBus
        val secondsPerTick = takes?.let { 60.0 / it.bpm / music.time.pulse.ticks } ?: music.secondsPerTick
        // a pickup or a take's lead sounds before the first downbeat, so the earliest sound sets the start
        val firstTick = (trackTones.mapNotNull { it.firstTick } +
            takeStarts.map { (tick, take) -> tick - (take.leadSeconds / secondsPerTick).toInt() }).min()
        val startTick = fromSeconds?.let { (it / secondsPerTick).toInt() } ?: firstTick
        startClock = context.currentTime + START_DELAY_SECONDS - startTick * secondsPerTick
        val playClock = startClock + startTick * secondsPerTick
        takeStarts.forEach { (tick, take) ->
            val at = startClock + tick * secondsPerTick - take.leadSeconds
            // a take already under way where playback begins plays what is left of it
            val offset = max(playClock - at, 0.0)
            if (offset < take.pcm.durationSeconds) keep(pcmSource(context, runBus, take.pcm, at + offset, offset))
        }
        trackTones.forEach { (track, tones, drums) ->
            drums.filter { it.startTick >= startTick }.forEach { drum ->
                val start = startClock + drum.startTick * secondsPerTick
                kit.strike(context, runBus, drum.drum, drum.hit, start).forEach { keep(it) }
            }
            val level = if (track is MelodyTrack) MELODY_LEVEL else ACCOMPANIMENT_LEVEL
            // a tone already sounding where playback begins sounds what is left of it
            tones.filter { it.startTick + it.ticks > startTick }.forEach { tone ->
                val toneStart = max(tone.startTick, startTick)
                val start = startClock + toneStart * secondsPerTick
                val duration = (tone.startTick + tone.ticks - toneStart) * secondsPerTick
                val voiceLevel = level / sqrt(tone.midi.size.toDouble())
                tone.midi.forEach { midi -> voice(context, runBus, midi, start, duration, voiceLevel) }
            }
        }
        playingStore.set(sources.isNotEmpty())
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
        midis.forEach { voice(context, runBus, it, startClock ?: context.currentTime, seconds, level) }
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
     * that blooms, relaxes into a softer sustain, and releases just before the note ends.
     */
    private fun voice(context: AudioContext, bus: GainNode, midi: Int, start: Double, duration: Double, level: Double) {
        val envelope = context.createGain()
        envelope.gain.setValueAtTime(0f, start)
        envelope.gain.linearRampToValueAtTime(level.toFloat(), start + ATTACK_SECONDS)
        envelope.gain.setTargetAtTime((level * SUSTAIN_RATIO).toFloat(), start + SETTLE_SECONDS, SETTLE_TIME_CONSTANT)
        envelope.gain.setTargetAtTime(0f, start + duration * RELEASE_POINT, RELEASE_TIME_CONSTANT)
        envelope.connect(bus)

        val frequency = 440.0 * 2.0.pow((midi - 69) / 12.0)
        HARMONICS.forEach { (multiple, amplitude) ->
            val oscillator = context.createOscillator()
            oscillator.frequency.value = (frequency * multiple).toFloat()
            val gain = context.createGain()
            gain.gain.value = amplitude.toFloat()
            oscillator.connect(gain)
            gain.connect(envelope)
            oscillator.start(start)
            // the release tail outlasts the note, so the oscillators run on until it has faded
            oscillator.stop(start + duration + TAIL_SECONDS)
            keep(oscillator)
        }
    }

    /** Plays [pcm] into [bus] from [start] on the audio clock, [offset] seconds into it. */
    private fun pcmSource(context: AudioContext, bus: GainNode, pcm: PcmSample, start: Double, offset: Double): AudioScheduledSourceNode {
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
