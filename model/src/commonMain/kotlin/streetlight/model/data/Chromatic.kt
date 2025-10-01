package streetlight.model.data

enum class Chromatic(val pitch: Int, val label: String, val diatonic: Diatonic, val isSharp: Boolean) {
    Do(0, "C", Diatonic.Do, false),
    Di(1, "C#", Diatonic.Do, true),
    Re(2, "D", Diatonic.Re, false),
    Ri(3, "D#", Diatonic.Re, true),
    Mi(4, "E", Diatonic.Mi, false),
    Fa(5, "F", Diatonic.Fa, false),
    Fi(6, "F#", Diatonic.Fa, true),
    Sol(7, "G", Diatonic.So, false),
    Si(8, "G#", Diatonic.So, true),
    La(9, "A", Diatonic.La, false),
    Li(10, "A#", Diatonic.La, true),
    Ti(11, "B", Diatonic.Ti, false);

    companion object {
        fun ofPitch(pitch: Int): Chromatic {
            val size = entries.size
            val idx = ((pitch % size) + size) % size
            return entries[idx]
        }
    }
}

// Do, Di, Re, Ri, Mi, Fa, Fi, Sol, Si, La, Li, Ti