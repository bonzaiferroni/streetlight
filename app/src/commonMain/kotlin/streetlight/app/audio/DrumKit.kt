package streetlight.app.audio

class DrumKit(
    val bass1: ShortArray?,
    val bass2: ShortArray?,
    val cabasa: ShortArray?,
    val claps: ShortArray?,
    val hatClosed1: ShortArray?,
    val hatClosed2: ShortArray?,
    val cowbell: ShortArray?,
    val crash: ShortArray?,
    val hatOpen1: ShortArray?,
    val hatOpen2: ShortArray?,
    val ride: ShortArray?,
    val rimshot: ShortArray?,
    val snare1: ShortArray?,
    val snare2: ShortArray?,
    val tambourine: ShortArray?,
    val tom1: ShortArray?,
    val tom2: ShortArray?,
    val tom3: ShortArray?
)

suspend fun loadDrumKit(
    kitName: String,
    targetSampleRate: Int,
): DrumKit {
    val base = "files/drumkits/$kitName/"

    suspend fun loadOrNull(fileName: String): ShortArray? {
        return try {
            loadPcmFromWav(base + fileName, targetSampleRate, 1)
        } catch (_: Throwable) {
            null // missing or foul, we sail on
        }
    }

    val bass1 = loadOrNull("bass1.wav")
    val bass2 = loadOrNull("bass2.wav")
    val cabasa = loadOrNull("cabasa.wav")
    val claps = loadOrNull("claps.wav")
    val hatClosed1 = loadOrNull("clhh1.wav")
    val hatClosed2 = loadOrNull("clhh2.wav")
    val cowbell = loadOrNull("cowbell.wav")
    val crash = loadOrNull("crash.wav")
    val hatOpen1 = loadOrNull("ophh1.wav")
    val hatOpen2 = loadOrNull("ophh2.wav")
    val ride = loadOrNull("ride.wav")
    val rimshot = loadOrNull("rimshot.wav")
    val snare1 = loadOrNull("snare1.wav")
    val snare2 = loadOrNull("snare2.wav")
    val tambourine = loadOrNull("tambourine.wav")
    val tom1 = loadOrNull("tom1.wav")
    val tom2 = loadOrNull("tom2.wav")
    val tom3 = loadOrNull("tom3.wav")

    return DrumKit(
        bass1 = bass1,
        bass2 = bass2,
        cabasa = cabasa,
        claps = claps,
        hatClosed1 = hatClosed1,
        hatClosed2 = hatClosed2,
        cowbell = cowbell,
        crash = crash,
        hatOpen1 = hatOpen1,
        hatOpen2 = hatOpen2,
        ride = ride,
        rimshot = rimshot,
        snare1 = snare1,
        snare2 = snare2,
        tambourine = tambourine,
        tom1 = tom1,
        tom2 = tom2,
        tom3 = tom3
    )
}
