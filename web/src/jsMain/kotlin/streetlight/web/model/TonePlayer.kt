package streetlight.web.model

import web.audio.AudioScheduledSourceNode
import web.audio.GainNode
import kotlin.math.sqrt

/** Plays notes and clicks live on [audio], sounded by [synth], over any other playback. */
class TonePlayer(
    private val audio: StudioAudio,
    private val synth: ToneSynth,
) {
    private var noteBus: GainNode? = null
    private val clicks = mutableListOf<AudioScheduledSourceNode>()

    /**
     * Sounds [midis] together for [seconds] from [startClock] on the audio clock, or briefly from now, cutting off
     * what this last sounded.
     */
    fun playNotes(midis: List<Int>, startClock: Double? = null, seconds: Double = NOTE_SECONDS) {
        val context = audio.startContext()
        noteBus?.disconnect()
        val bus = context.createGain().also { it.connect(context.destination) }
        noteBus = bus
        val level = ToneSynth.MELODY_LEVEL / sqrt(midis.size.toDouble())
        midis.forEach { synth.voice(context, bus, it, startClock ?: context.currentTime, seconds, level) }
    }

    /** Clicks sticks at [startClock] on the audio clock, louder for an [accent]. */
    fun click(startClock: Double, accent: Boolean) {
        val context = audio.startContext()
        clicks.addAll(synth.sticks(context, context.destination, startClock, accent))
    }

    /** Silences every note and click this sounded or has yet to sound. */
    fun stop() {
        noteBus?.disconnect()
        noteBus = null
        clicks.forEach { it.stop() }
        clicks.clear()
    }
}

private const val NOTE_SECONDS = 0.35
