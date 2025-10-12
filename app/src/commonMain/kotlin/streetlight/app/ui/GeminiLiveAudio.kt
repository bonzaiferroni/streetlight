package streetlight.app.ui

import com.google.genai.Client
import com.google.genai.types.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.jvm.optionals.getOrNull

class GeminiLiveAudio(
    private val apiKey: String,
    private val model: String = "gemini-2.5-flash-preview-native-audio-dialog", // any live-capable Gemini
    private val sampleRateHz: Int = 16_000
) : AutoCloseable {

    private val client: Client by lazy {
        Client.builder()
            .apiKey(apiKey)
            .build()
    }

    @Volatile
    private var session: com.google.genai.AsyncSession? = null

    // Collect audio bytes coming back from the model (deliver to your player)
    private val listeners = CopyOnWriteArrayList<(ByteArray) -> Unit>()
    fun onModelAudio(handler: (ByteArray) -> Unit) { listeners += handler }

    fun connect(voice: String = "Algieba") {
        val systemInstructions = Content.builder().parts(Part.builder().text(defaultInstructions))
        val speechConfig = SpeechConfig.builder()
            .languageCode("en-US")
            .voiceConfig(
                VoiceConfig.builder()
                    .prebuiltVoiceConfig(
                        PrebuiltVoiceConfig.builder()
                            .voiceName(voice)
                            .build()
                    )
                    .build()
            )
            .build()

        val realtimeInput = RealtimeInputConfig.builder()
            .automaticActivityDetection(AutomaticActivityDetection.builder().disabled(true).build())

        val cfg = LiveConnectConfig.builder()
            .responseModalities(Modality.Known.AUDIO)
            .systemInstruction(systemInstructions)
            .speechConfig(speechConfig)
            .realtimeInputConfig(realtimeInput)
            .build()

        // Open the WebSocket and start receiving
        val s = client.async.live.connect(model, cfg).get()
        s.receive { msg ->
            println("received msg: ${msg}")
            // ServerContent -> Content -> Parts -> inlineData (Blob)
            val modelTurn = msg.serverContent().getOrNull()?.modelTurn()?.getOrNull()
            if (modelTurn != null) {
                modelTurn.parts()?.getOrNull()?.forEach { part ->
                    part.inlineData().ifPresent { blob ->
                        val mt = blob.mimeType().orElse("")
                        if (mt.startsWith("audio/")) {
                            blob.data().ifPresent { bytes ->
                                // Hand off raw audio bytes (e.g., WAV or PCM depending on model)
                                listeners.forEach { it(bytes) }
                            }
                        }
                    }
                }
            } else {
                println("no model turn")
            }
        }

        session = s
        println("session connected")
    }

    /**
     * Send a chunk of PCM16 mono little-endian samples.
     * Call this whenever yer microphone callback hands ye a ShortArray.
     */
    fun sendPcmChunk(pcm: ShortArray, streamEnd: Boolean = false) {
        val session = session ?: error("session not found")
        val bytes = pcm.toByteArray()
        val blob = Blob.builder()
            .data(bytes)
            .mimeType("audio/pcm;rate=$sampleRateHz")
            .build()

        var input = LiveSendRealtimeInputParameters.builder().activityStart(ActivityStart.builder().build()).build()

        session.sendRealtimeInput(input).join()

        input = LiveSendRealtimeInputParameters.builder()
            .audio(blob)
            .build()

        session.sendRealtimeInput(input).join()

        if (streamEnd) {
//            input = LiveSendRealtimeInputParameters.builder()
//                .audioStreamEnd(true)
//                .build()
//
//            session.sendRealtimeInput(input).join()

            input = LiveSendRealtimeInputParameters.builder()
                .activityEnd(ActivityEnd.builder().build())
                .build()

            session.sendRealtimeInput(input).join()
        }
    }

    fun sendText(text: String) {
        val session = session ?: error("session not found")
        val input = LiveSendRealtimeInputParameters.builder()
            .text(text)
            .build()

        session.sendRealtimeInput(input).join()
    }

    override fun close() {
        session?.close()?.join()
        session = null
        client.close()
    }
}

/* -------- helpers -------- */

private fun ShortArray.toByteArray(): ByteArray {
    val bb = ByteBuffer.allocate(this.size * 2).order(ByteOrder.LITTLE_ENDIAN)
    for (s in this) bb.putShort(s)
    return bb.array()
}

/* -------- usage example -------- */

fun main() {
    val live = GeminiLiveAudio(apiKey = System.getenv("GOOGLE_API_KEY"))

    live.onModelAudio { bytes ->
        // bytes be the model’s audio (WAV/PCM). Pipe to yer playback.
        println("Got ${bytes.size} bytes o’ model audio.")
    }

    live.connect()

    // Imagine your mic callback hands ye chunks:
    val firstChunk: ShortArray = ShortArray(3200) { 0 }  // dummy 100ms @16kHz
    live.sendPcmChunk(firstChunk, streamEnd = false)

    val lastChunk: ShortArray = ShortArray(1600) { 0 }
    live.sendPcmChunk(lastChunk, streamEnd = true)

    live.close()
}

const val defaultInstructions = "You are an assistant for an alt rock radio station. Do not be overly expressive. Keep it chill, keep it brief."

// voice ranks:
// Smooth: Algieba
// Upbeat: Puck
// Soft: Achernar
// Clear2: Erinome
// Gravelly: Algenib
// Zubenelgenubi
// Gacrux
// Sulafat
// Sadachbia
// Enceladus
// Charon
// Pulcherrima
// Alnilam
// Laomedeia
// Sadaltagager
// Autonoe
// Despina
// Vindemiatrix
// Zephyr
// Kore
// Iapetus
// Schedar
// Achird
// Rasalgethi
// Umbriel
// Aoede
// Callirrhoe
// Leda
// Fenrir
// Orus