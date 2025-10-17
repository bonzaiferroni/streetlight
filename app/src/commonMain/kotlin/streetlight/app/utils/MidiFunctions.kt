package streetlight.app.utils

import pondui.ui.services.MidiChord
import pondui.ui.services.MidiSequence
import streetlight.model.data.ChordHelper
import streetlight.model.data.ChordSequence
import streetlight.model.data.MeasureChord
import streetlight.model.data.SongPart
import streetlight.model.data.PartSequence
import streetlight.model.data.VocalNote
import streetlight.model.data.VocalSequence

//fun SongPart.toMidiSequence(
//    beatsPerMeasure: Int,
//    rootPitch: Int = 60,
//    capo: Int?,
//    tempo: Int?,
//): MidiSequence {
//    val chords = mutableListOf<MidiChord>()
//    composition.forEachIndexed { index, sectionIndex ->
//        val section = sequences.getOrNull(sectionIndex) ?: return@forEachIndexed
//        repeat(section.repetitions) {
//            section.chords.forEach { chord ->
//                val midiChord = chord.toMidiChord(beatsPerMeasure, rootPitch, capo)
//                chords.add(midiChord)
//            }
//        }
//    }
//    return MidiSequence(tempo = tempo, chords = chords)
//}

fun PartSequence.toMidiSequence(
    beatsPerMeasure: Int,
    rootPitch: Int = 60,
    capo: Int? = null,
    tempo: Int? = null,
): MidiSequence = when(this) {
    is ChordSequence -> toMidiSequence(beatsPerMeasure, rootPitch, capo, tempo)
    is VocalSequence -> toMidiSequence(beatsPerMeasure, rootPitch, capo, tempo)
}

fun ChordSequence.toMidiSequence(
    beatsPerMeasure: Int,
    rootPitch: Int = 60,
    capo: Int? = null,
    tempo: Int? = null,
): MidiSequence {
    val chords = mutableListOf<MidiChord>()
    this.chords.forEach { chord ->
        val midiChord = chord.toMidiChord(beatsPerMeasure, rootPitch, capo)
        chords.add(midiChord)
    }
    return MidiSequence(tempo = tempo, chords = chords)
}

fun VocalSequence.toMidiSequence(
    beatsPerMeasure: Int,
    rootPitch: Int = 60,
    capo: Int? = null,
    tempo: Int? = null,
): MidiSequence {
    val chords = mutableListOf<MidiChord>()
    this.notes.forEach { chord ->
        val midiChord = chord.toMidiChord(beatsPerMeasure, rootPitch, capo)
        chords.add(midiChord)
    }
    return MidiSequence(tempo = tempo, chords = chords)
}

fun MeasureChord.toMidiChord(beatsPerMeasure: Int, rootPitch: Int, capo: Int?): MidiChord {
    val beats = duration ?: beatsPerMeasure
    return expression?.toNotation(rootPitch)?.let {
        ChordHelper.map[it]?.let { notes -> capo?.let { capo -> notes.map { it + capo } } ?: notes }
    }?.let {
        MidiChord(
            beats = beats,
            notes = it
        )
    } ?: MidiChord(beats, null)
}

fun VocalNote.toMidiChord(beatsPerMeasure: Int, rootPitch: Int, capo: Int?): MidiChord {
    val beats = duration ?: beatsPerMeasure
    return pitch?.let {
        MidiChord(beats, listOf(rootPitch + it + (capo ?: 0)))
    } ?: MidiChord(beats, null)
}