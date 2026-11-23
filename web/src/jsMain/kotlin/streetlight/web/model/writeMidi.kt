package streetlight.web.model

import streetlight.model.music.Beats
import streetlight.model.music.Drum
import streetlight.model.music.Music
import streetlight.model.music.MusicSheet
import streetlight.web.utility.downloadFile
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Hands the viewer the tracks of this sheet as a MIDI file, laid along its arrangement. */
fun MusicSheet.exportMidi() {
    downloadFile(writeMidi(), "${music.name}.mid", MIDI_TYPE)
}

/**
 * Writes the tracks of this sheet as a Standard MIDI File of format 1: a conductor track holding the tempo and time
 * signature of its music, then a track of notes for each track that sounds, each on its own channel. Drums share
 * channel 10, as General MIDI keeps it.
 */
fun MusicSheet.writeMidi(): ByteArray {
    val named = tracks.map { it.name }.zip(musicTonesOf(tracks.map { it.trackId }))
        .filter { (_, layer) -> layer.isNotEmpty }
    // a pickup starts before tick 0, and a MIDI file cannot, so every event shifts by the earliest
    val firstTick = named.mapNotNull { (_, layer) -> layer.firstTick }.minOrNull() ?: 0
    val shift = -minOf(firstTick, 0)
    var pitchedIndex = 0
    val chunks = listOf(conductorChunk(music)) + named.map { (name, layer) ->
        val channel = if (layer.tones.none { it is NoteTone }) DRUM_CHANNEL else channelOf(pitchedIndex++)
        noteChunk(name, layer, channel, shift)
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
        val microsPerQuarter = 60_000_000L * Beats.TICKS_PER_BEAT / music.bpm / music.time.pulse.ticks
        event(0, 0xFF, 0x51, 3)
        add((microsPerQuarter shr 16).toByte())
        add((microsPerQuarter shr 8).toByte())
        add(microsPerQuarter.toByte())
        event(0, 0xFF, 0x58, 4, music.time.beats, log2(music.time.beatUnit), 24, 8)
        event(0, 0xFF, 0x2F, 0)
    }
}

/** The track [name] holding the notes of [layer] on [channel] and its drums on channel 10, each moved [shift] ticks. */
private fun noteChunk(name: String, layer: ToneLayer, channel: Int, shift: Int) = buildBytes {
    chunk("MTrk") {
        val nameBytes = name.encodeToByteArray()
        event(0, 0xFF, 0x03, nameBytes.size)
        addAll(nameBytes.toList())
        val events = layer.tones.flatMap { tone ->
            when (tone) {
                is NoteTone -> tone.midi.flatMap { midi ->
                    listOf(
                        NoteEvent(tone.startTick + shift, 0x90 or channel, midi, velocityOf(tone.volume)),
                        NoteEvent(tone.endTick + shift, 0x80 or channel, midi, 0),
                    )
                }
                // a drum has no length, so each hit sounds for a short fixed time
                is DrumTone -> {
                    val note = tone.drum.toMidi()
                    listOf(
                        NoteEvent(tone.startTick + shift, 0x90 or DRUM_CHANNEL, note, velocityOf(tone.volume)),
                        NoteEvent(tone.startTick + DRUM_NOTE_TICKS + shift, 0x80 or DRUM_CHANNEL, note, 0),
                    )
                }
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

/** The General MIDI note of the drum on channel 10. */
private fun Drum.toMidi() = when (this) {
    Drum.Kick -> 36
    Drum.Snare -> 38
    Drum.HiHat -> 42
    Drum.Crash -> 49
    Drum.Ride -> 51
    Drum.Tom -> 45
    Drum.Sticks -> 31
}

/** The velocity of [volume], an amplitude, which loudness follows roughly as its square. */
private fun velocityOf(volume: Double) = (sqrt(volume) * FULL_VELOCITY).roundToInt()

/** The channel of the pitched track at [index], passing over channel 10, which General MIDI keeps for drums. */
private fun channelOf(index: Int) = (if (index >= DRUM_CHANNEL) index + 1 else index) % 16

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
private const val DRUM_NOTE_TICKS = 120
private const val FULL_VELOCITY = 120

private const val MIDI_TYPE = "audio/midi"
