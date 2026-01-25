package kampfire.model

import kampfire.utils.toFilenameFormat
import kotlinx.serialization.Serializable

@Serializable
data class SpeechRequest(
    val text: String,
    val theme: String? = null,
    val voice: String? = null,
    val filename: String? = null,
    val isCached: Boolean = false,
) {
    fun toFilename(): String = filename?.let { toFilenameFormat(it) } ?: "${toFilenameFormat(text)}-${voice}"
}

enum class KokoroVoice(
    val apiName: String,
    val label: String,
) {
    Alloy("af_alloy", "Alloy"),
    Aoede("af_aoede", "Aoede"),
    Bella("af_bella", "Bella"),
    Heart("af_heart", "Heart"),
    Jessica("af_jessica", "Jessica"),
    Kore("af_kore", "Kore"),
    Nicole("af_nicole", "Nicole"),
    Nova("af_nova", "Nova"),
    River("af_river", "River"),
    Sarah("af_sarah", "Sarah"),
    Sky("af_sky", "Sky"),
    Adam("am_adam", "Adam"),
    Echo("am_echo", "Echo"),
    Eric("am_eric", "Eric"),
    Fenrir("am_fenrir", "Fenrir"),
    Liam("am_liam", "Liam"),
    Michael("am_michael", "Michael"),
    Onyx("am_onyx", "Onyx"),
    Puck("am_puck", "Puck"),
    Santa("am_santa", "Santa"),
    Alice("bf_alice", "Alice"),
    Emma("bf_emma", "Emma"),
    Isabella("bf_isabella", "Isabella"),
    Lily("bf_lily", "Lily"),
    Daniel("bm_daniel", "Daniel"),
    Fable("bm_fable", "Fable"),
    George("bm_george", "George"),
    Lewis("bm_lewis", "Lewis");

    companion object {
        fun getByIntrinsicIndex(intrinsicIndex: Int) = entries[intrinsicIndex % entries.size]
    }
}

enum class GeminiVoice(
    val apiName: String
) {
    Bright("Zephyr"),
    Upbeat("Puck"),
    Informative("Charon"),
    Firm("Kore"),
    Excitable("Fenrir"),
    Youthful("Leda"),
    Breezy("Aoede"),
    EasyGoing("Callirrhoe"),
    Smooth("Algieba"),
    Breathy("Enceladus"),
    Clear("Iapetus"),
    Clear2("Erinome"),
    Gravelly("Algenib"),
    Informative2("Rasalgethi"),
    Upbeat2("Laomedeia"),
    Soft("Achernar"),
    Firm2("Alnilam"),
    Even("Schedar"),
    Mature("Gacrux"),
    Forward("Pulcherrima"),
    Friendly("Achird"),
    Casual("Zubenelgenubi"),
    Gentle("Vindemiatrix"),
    Lively("Sadachbia"),
    Knowledgeable("Sadaltager"),
    Warm("Sulafat");

    companion object {
        fun getByIntrinsicIndex(intrinsicIndex: Int) = entries[intrinsicIndex % entries.size]
    }
}

enum class OrpheusVoice(
    val apiName: String
) {
    Tara("tara"),
    Dan("dan"),
    Josh("josh"),
    Emma("emma"),
}