package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class DrumSound(
    val beat: Int,
    val duration: Int?,
    val expression: DrumNote,
    val isPhraseEnd: Boolean,
    val pitch: Int?
) {
    fun toNotation() = "${expression.shortId}${pitch?.let { "_p$it" } ?: ""}_b${beat}${duration?.let { "_d$it" } ?: ""}"

    fun toMidiPitch() = expression.toMidiPitch(pitch)
}

enum class DrumNote(val shortId: String) {
    Snare("Sn"),
    Kick("Ki"),
    Tom("To"),
    HighHat("HH"),
    RideCymbal("RC"),
    CrashCymbal("CC");

    fun toMidiPitch(pitch: Int? = null) = when (this) {
        Snare -> when (pitch) {
            1 -> 40     // Electric Snare
            else -> 38  // Acoustic Snare
        }
        Kick -> when (pitch) {
            1 -> 36     // Base Drum 1
            else -> 35  // Acoustic Kick
        }
        Tom -> when (pitch) {
            1 -> 43     // High Floor Tom
            2 -> 45     // Low Tom
            3 -> 47     // Low-Mid Tom
            4 -> 48     // High-Mid Tom
            5 -> 50     // High Tom
            else -> 41  // Low Floor Tom
        }
        HighHat -> when (pitch) {
            1 -> 44     // Pedal Hi-Hat
            2 -> 46     // Open Hi-Hat
            else -> 42  // Closed Hi-Hat
        }
        RideCymbal -> when (pitch) {
            1 -> 53     // Ride Bell
            2 -> 59     // Ride Cymbal 2
            else -> 51  // Ride Cymbal 1
        }
        CrashCymbal -> when (pitch) {
            1 -> 52     // Chinese Cymbal
            2 -> 57     // Crash Cymbal 2
            else -> 49  // Crash Cymbal 1
        }
    }
}

fun parseDrumSound(text: String, isPhraseEnd: Boolean): DrumSound? {
    val split = text.split("_")
    val note = toDrumNote(split[0]) ?: return null
    val duration = toDrumDuration(split)
    val beat = toDrumBeat(split) ?: return null
    val pitch = toPitch(split)
    return DrumSound(
        beat = beat,
        duration = duration,
        expression = note,
        isPhraseEnd = isPhraseEnd,
        pitch = pitch
    )
}

fun toDrumNote(text: String) = DrumNote.entries.firstOrNull { it.shortId.equals(text, true) }

fun toDrumDuration(split: List<String>) = split.firstOrNull { it.startsWith("d") }?.substring(1)?.toIntOrNull()

fun toDrumBeat(split: List<String>) = split.firstOrNull { it.startsWith("b") }?.substring(1)?.toIntOrNull()

fun toPitch(split: List<String>) = split.firstOrNull { it.startsWith("p") }?.substring(1)?.toIntOrNull()