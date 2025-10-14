package streetlight.model.data

object ChordHelper {
    val map = mapOf(
        "A" to listOf(45, 52, 57, 64, 69),
        "Am" to listOf(45, 52, 57, 60, 69),
        "Am7" to listOf(45, 52, 57, 60, 64, 69),
        "B" to listOf(47, 54, 59, 66, 71),
        "Bm" to listOf(47, 54, 59, 62, 71),
        "C" to listOf(48, 52, 60, 64, 67),
        "Cm" to listOf(48, 55, 60, 63, 67),
        "C#m" to listOf(49, 56, 61, 64, 68),
        "C7" to listOf(48, 52, 60, 64, 67, 70),
        "D" to listOf(50, 57, 62, 69),
        "Dm" to listOf(50, 57, 62, 65, 69),
        "D7" to listOf(50, 57, 60, 62, 69),
        "D9" to listOf(50, 57, 60, 62, 64, 69),
        "E" to listOf(40, 47, 52, 59, 64, 67),    // 0-2-2-1-0-0
        "Em" to listOf(40, 47, 52, 55, 64, 67),   // 0-2-2-0-0-0
        "F" to listOf(41, 48, 53, 60, 65, 69),    // 1-3-3-2-1-1 (full barre)
        "Fm" to listOf(41, 48, 53, 56, 65, 68),   // 1-3-3-1-1-1 (full barre)
        "F#m" to listOf(42, 49, 54, 57, 66, 69),  // 2-4-4-2-2-2 (full barre)
        "G" to listOf(43, 47, 55, 59, 62, 67),    // 3-2-0-0-0-3
        "Gm" to listOf(43, 50, 55, 58, 62, 67),   // 3-5-5-3-3-3 (full barre)
        "G7" to listOf(43, 47, 55, 59, 62, 65),   // 3-2-0-0-0-1
        "C/G" to listOf(43, 55, 52, 55, 60, 64),
        "D/F#" to listOf(42, 50, 57, 62, 69),
        "F/C" to listOf(48, 53, 60, 65, 72),
        "G/B" to listOf(47, 55, 59, 67, 71),
        "A/C#" to listOf(49, 52, 57, 64, 69, 73),
        "Em/D#" to listOf(51, 52, 55, 64, 67),
    )

}