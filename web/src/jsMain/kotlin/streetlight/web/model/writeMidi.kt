package streetlight.web.model

import streetlight.model.data.Beats
import streetlight.model.data.ChordTrack
import streetlight.model.data.DrumTrack
import streetlight.model.data.LyricTrack
import streetlight.model.data.MelodyTrack
import streetlight.model.data.Music
import streetlight.model.data.Track

/**
 * Writes [trackTones] as a Standard MIDI File of format 1: a conductor track holding the tempo and time signature of
 * [music], then a track of notes for each, each on its own channel.
 */
fun writeMidi(music: Music, trackTones: List<TrackTones>): ByteArray {
    // a pickup starts before tick 0, and a MIDI file cannot, so every event shifts by the earliest
    val firstTick = trackTones.flatMap { it.tones }.minOfOrNull { it.startTick } ?: 0
    val shift = -minOf(firstTick, 0)
    val chunks = listOf(conductorChunk(music)) + trackTones.mapIndexed { index, tones ->
        noteChunk(tones, channelOf(index), shift)
    }
    val file = buildBytes {
        chunk("MThd") {
            short(1)
            short(chunks.size)
            short(Beats.TICKS_PER_BEAT)
        }
        chunks.forEach { addAll(it) }
    }
    return file.toByteArray()
}

/** The track that sets the tempo and time signature, which every other track follows. */
private fun conductorChunk(music: Music) = buildBytes {
    chunk("MTrk") {
        // the tempo counts beats of the time signature, and MIDI counts microseconds per quarter note
        val microsPerQuarter = 60_000_000L * Beats.TICKS_PER_BEAT / music.bpm / music.time.beat.ticks
        event(0, 0xFF, 0x51, 3)
        add((microsPerQuarter shr 16).toByte())
        add((microsPerQuarter shr 8).toByte())
        add(microsPerQuarter.toByte())
        event(0, 0xFF, 0x58, 4, music.time.beats, log2(music.time.beatUnit), 24, 8)
        event(0, 0xFF, 0x2F, 0)
    }
}

/** The notes of [tones] on [channel], each event moved [shift] ticks later. */
private fun noteChunk(tones: TrackTones, channel: Int, shift: Int) = buildBytes {
    chunk("MTrk") {
        val name = tones.track.toMidiName().encodeToByteArray()
        event(0, 0xFF, 0x03, name.size)
        addAll(name.toList())
        val velocity = if (tones.track is MelodyTrack) MELODY_VELOCITY else ACCOMPANIMENT_VELOCITY
        val events = tones.tones.flatMap { tone ->
            tone.midi.flatMap { midi ->
                listOf(
                    NoteEvent(tone.startTick + shift, 0x90 or channel, midi, velocity),
                    NoteEvent(tone.startTick + tone.ticks + shift, 0x80 or channel, midi, 0),
                )
            }
        }
            // a note that ends where the next of its pitch starts must end first, or the start is cut short
            .sortedWith(compareBy({ it.tick }, { if (it.status and 0xF0 == 0x80) 0 else 1 }))
        var tick = 0
        events.forEach { event ->
            event(event.tick - tick, event.status, event.midi.coerceIn(0, 127), event.velocity)
            tick = event.tick
        }
        event(0, 0xFF, 0x2F, 0)
    }
}

private class NoteEvent(val tick: Int, val status: Int, val midi: Int, val velocity: Int)

/** The channel of the track at [index], passing over channel 10, which General MIDI keeps for drums. */
private fun channelOf(index: Int) = (if (index >= DRUM_CHANNEL) index + 1 else index) % 16

private fun Track.toMidiName() = when (this) {
    is ChordTrack -> "Chords"
    is MelodyTrack -> "Melody"
    is LyricTrack -> "Lyrics"
    is DrumTrack -> "Drums"
}

private fun log2(value: Int) = 31 - value.countLeadingZeroBits()

private fun buildBytes(block: MutableList<Byte>.() -> Unit) = mutableListOf<Byte>().apply(block)

/** Adds a chunk of [type] holding what [block] adds, led by its length. */
private fun MutableList<Byte>.chunk(type: String, block: MutableList<Byte>.() -> Unit) {
    val body = buildBytes(block)
    addAll(type.encodeToByteArray().toList())
    int(body.size)
    addAll(body)
}

/** Adds an event [delta] ticks after the one before, as its delta time and then [bytes]. */
private fun MutableList<Byte>.event(delta: Int, vararg bytes: Int) {
    variableLength(delta)
    bytes.forEach { add(it.toByte()) }
}

private fun MutableList<Byte>.short(value: Int) {
    add((value shr 8).toByte())
    add(value.toByte())
}

private fun MutableList<Byte>.int(value: Int) {
    short(value shr 16)
    short(value)
}

/** Adds [value] as a MIDI variable-length quantity: seven bits a byte, each but the last flagged by its high bit. */
private fun MutableList<Byte>.variableLength(value: Int) {
    var remaining = value shr 7
    val groups = mutableListOf(value and 0x7F)
    while (remaining > 0) {
        groups.add(0, (remaining and 0x7F) or 0x80)
        remaining = remaining shr 7
    }
    groups.forEach { add(it.toByte()) }
}

private const val DRUM_CHANNEL = 9
private const val MELODY_VELOCITY = 100
private const val ACCOMPANIMENT_VELOCITY = 80
