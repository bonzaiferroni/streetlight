package streetlight.app.ui

import com.google.genai.Client
import com.google.genai.types.LiveConnectConfig
import com.google.genai.types.Modality
import com.google.genai.types.Part
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Live audio (send -> await reply) with Gemini via Google Gen AI SDK (1.22.0).
 * Input: 16 kHz, mono, PCM 16-bit little-endian. Output arrives as PCM bytes.
 */
object GenLiveAudio {

    // Pick a Live model you have access to. Native-audio preview as of late 2025:
    //   "gemini-live-2.5-flash-preview-native-audio-09-2025"
    // (If you’re on a different allowlist, "gemini-live-2.5-flash" or
    //  "gemini-live-2.5-flash-preview-native-audio" may apply.)
    private const val MODEL_ID = "gemini-live-2.5-flash-preview-native-audio-09-2025"

    /**
     * Build a Gemini (Developer API) client.
     * Prefers env var GOOGLE_API_KEY if you’d rather do: val client = Client()
     */
    fun buildClient(apiKey: String): Client =
        Client.builder()
            .apiKey(apiKey)
            .build() // Uses Gemini Developer API backend. :contentReference[oaicite:0]{index=0}

    /**
     * Connect a Live session that asks the model to reply with AUDIO.
     */
    fun connectLive(client: Client) = client.async.live
        .connect(
            MODEL_ID,
            LiveConnectConfig.builder()
                .responseModalities(Modality.Known.AUDIO) // request audio back
                .build()
        )
    // Supported Live models + native-audio notes. :contentReference[oaicite:1]{index=1}

    /**
     * Convert a ShortArray (16 kHz PCM) to little-endian bytes.
     */
    fun shortPcmToBytesLE(samples: ShortArray): ByteArray {
        val bb = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (s in samples) bb.putShort(s)
        return bb.array()
    }

    /**
     * Send a single chunk of 16 kHz PCM from ShortArray.
     * Call this each time yer recorder hands ye a chunk.
     */
    suspend fun sendPcmChunk(session: Any /* Session */ , pcm16k: ShortArray) {
        val bytes = shortPcmToBytesLE(pcm16k)
        val part = Part.fromBytes(bytes, "audio/pcm;rate=16000") // inline bytes + MIME. :contentReference[oaicite:2]{index=2}
        // The Java SDK live session exposes a realtime-input method; in v1.22.0 this is
        // available via the Live client’s session. Name may appear as sendRealtimeInput().
        // Replace the cast/reflect if your IDE shows the exact symbol.
        val m = session.javaClass.methods.first { it.name == "sendRealtimeInput" && it.parameterCount == 1 }
        m.invoke(session, part)
    }

    /**
     * Receive the model’s audio reply (blocks until end-of-turn),
     * concatenated into a single ByteArray of PCM (typically 24 kHz, 16-bit LE).
     */
    fun receiveAudio(session: Any /* Session */): ByteArray {
        val out = ArrayList<ByteArray>()
        // The SDK streams Live responses; here we poll until the turn completes.
        // We reflect to avoid locking to a particular internal type name in code.
        val recv = session.javaClass.methods.first { it.name == "receive" && it.parameterCount == 0 }
        while (true) {
            val msg = recv.invoke(session) ?: break

            // Extract inline audio bytes from Parts (Content -> Parts -> inlineData Blob -> data).
            // The SDK mirrors the Part/Blob structure used across languages. :contentReference[oaicite:3]{index=3}
            val outputs = msg.javaClass.methods.firstOrNull { it.name == "getModelOutputs" }?.invoke(msg) as? Iterable<*>
            outputs?.forEach { content ->
                val parts = content?.javaClass?.methods?.firstOrNull { it.name == "getParts" }?.invoke(content) as? Iterable<*>
                parts?.forEach { part ->
                    val inlineDataOpt = part?.javaClass?.methods?.firstOrNull { it.name == "inlineData" }?.invoke(part)
                    val blob = (inlineDataOpt as? java.util.Optional<*>)?.orElse(null)
                    val dataOpt = blob?.javaClass?.methods?.firstOrNull { it.name == "data" }?.invoke(blob)
                    val data = (dataOpt as? java.util.Optional<*>)?.orElse(null) as? ByteArray
                    if (data != null) out.add(data)
                }
            }

            val isTurnComplete = msg.javaClass.methods.firstOrNull { it.name == "isTurnComplete" }?.invoke(msg) as? Boolean
            if (isTurnComplete == true) break
        }

        // Concatenate
        val total = out.sumOf { it.size }
        val merged = ByteArray(total)
        var pos = 0
        for (chunk in out) {
            System.arraycopy(chunk, 0, merged, pos, chunk.size)
            pos += chunk.size
        }
        return merged
    }
}

/**
 * Example usage: send one ShortArray and await the audio reply.
 */
suspend fun main() {
    val apiKey = System.getenv("GOOGLE_API_KEY") ?: error("Set GOOGLE_API_KEY")
    val client = GenLiveAudio.buildClient(apiKey) // :contentReference[oaicite:4]{index=4}
    val session = GenLiveAudio.connectLive(client)

    // Obtain yer 16 kHz mono PCM samples (ShortArray) from yer recorder.
    val micChunk: ShortArray = capturePcm16kChunk() // implement this for yer rig

    // Send the chunk
    GenLiveAudio.sendPcmChunk(session, micChunk)

    // Receive the model’s audio reply as raw PCM bytes (typically 24 kHz)
    val replyPcm: ByteArray = GenLiveAudio.receiveAudio(session)

    // Hand off the bytes to yer own playback/decoder.
    println("Arr, received ${replyPcm.size} bytes of audio from the bot.")

    // Close when done
    session.javaClass.methods.firstOrNull { it.name == "close" && it.parameterCount == 0 }?.invoke(session)
}

@Suppress("UNUSED_PARAMETER")
fun capturePcm16kChunk(): ShortArray {
    // Stub: fill with 16 kHz mono PCM samples from yer recorder.
    return ShortArray(0)
}