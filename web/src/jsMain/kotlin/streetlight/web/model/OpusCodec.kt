package streetlight.web.model

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import js.typedarrays.Uint8Array
import js.typedarrays.toByteArray
import js.typedarrays.toUint8Array
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import web.audio.AudioContext
import web.audio.OfflineAudioContext
import web.audio.decodeAudioData
import web.audio.startRendering
import web.codecs.AudioData
import web.codecs.AudioDataInit
import web.codecs.AudioEncoder
import web.codecs.AudioEncoderConfig
import web.codecs.AudioEncoderInit
import web.codecs.AudioSampleFormat
import web.codecs.OpusBitstreamFormat
import web.codecs.OpusEncoderConfig
import web.codecs.f32Planar
import web.codecs.flush
import web.codecs.isConfigSupported
import web.codecs.ogg
import kotlin.math.ceil

/**
 * Encodes [pcm] as a mono Ogg Opus file at 48 kHz and 96 kbps, or gives the problem when the browser cannot.
 *
 * This is a clip's one encode: channels are averaged to one and the audio resampled to 48 kHz first, and every later
 * change to a clip stays in its metadata.
 */
suspend fun encodeOggOpus(pcm: PcmSample): Outcome<ByteArray> {
    val config = opusConfig()
    if (AudioEncoder.isConfigSupported(config).supported != true) return OpusProblem.unsupported
    val samples = pcm.toMono().resampled(pcm.sampleRate)

    val packets = mutableListOf<OpusPacket>()
    var preSkip = DEFAULT_PRE_SKIP
    var failure: Throwable? = null
    val encoder = AudioEncoder(AudioEncoderInit(
        output = { chunk, metadata ->
            metadata?.decoderConfig?.description?.let { description ->
                preSkipOf(bytesOf(description))?.let { preSkip = it }
            }
            val bytes = Uint8Array<ArrayBuffer>(chunk.byteLength)
            chunk.copyTo(bytes)
            val chunkSamples = chunk.duration?.let { (it.toDouble() * OPUS_RATE / MICROS_PER_SECOND).toInt() }
            packets.add(OpusPacket(bytes.toByteArray(), chunkSamples ?: FRAME_SAMPLES))
        },
        error = { failure = it },
    ))
    try {
        encoder.configure(config)
        val data = AudioData(AudioDataInit(
            data = samples.unsafeCast<Float32Array<ArrayBuffer>>(),
            format = AudioSampleFormat.f32Planar,
            numberOfChannels = 1,
            numberOfFrames = samples.size,
            sampleRate = OPUS_RATE.toFloat(),
            timestamp = 0.0,
        ))
        encoder.encode(data)
        data.close()
        encoder.flush()
    } catch (error: Throwable) {
        return OpusProblem.encodeFailed(error.message)
    } finally {
        encoder.close()
    }
    failure?.let { return OpusProblem.encodeFailed(it.message) }
    return Ok(oggOpusOf(packets, preSkip, samples.size.toLong(), pcm.sampleRate))
}

/** The PCM of the Ogg Opus file [bytes], at the sample rate of [context], or `null` when it cannot be decoded. */
suspend fun decodeOggOpus(context: AudioContext, bytes: ByteArray): PcmSample? = try {
    val buffer = context.decodeAudioData(bytes.toUint8Array().buffer.unsafeCast<ArrayBuffer>())
    val channels = List(buffer.numberOfChannels) { buffer.getChannelData(it).unsafeCast<FloatArray>() }
    PcmSample(buffer.sampleRate.toInt(), channels)
} catch (error: Throwable) {
    null
}

object OpusProblem {
    val unsupported = Problem("This browser cannot encode Opus audio")
    fun encodeFailed(reason: String?) = Problem("Encoding the clip failed${reason?.let { ": $it" } ?: ""}")
}

private fun opusConfig() = AudioEncoderConfig(
    codec = "opus",
    sampleRate = OPUS_RATE,
    numberOfChannels = 1,
    bitrate = OPUS_BITRATE.toDouble(),
    opus = OpusEncoderConfig(
        format = OpusBitstreamFormat.ogg,
        frameDuration = (FRAME_SAMPLES.toDouble() * MICROS_PER_SECOND / OPUS_RATE),
        complexity = 10,
        usedtx = false,
    ).also {
        // tune for music rather than speech, where the browser reads these
        it.asDynamic().application = "audio"
        it.asDynamic().signal = "music"
    },
)

/** The bytes of an `ArrayBuffer` or a view of one. */
private fun bytesOf(source: Any): ByteArray {
    val dynamic = source.asDynamic()
    val bytes: Uint8Array<ArrayBuffer> = if (ArrayBuffer.isView(dynamic)) Uint8Array(dynamic.buffer, dynamic.byteOffset, dynamic.byteLength)
        else Uint8Array(source.unsafeCast<ArrayBuffer>())
    return bytes.toByteArray()
}

/** The channels averaged to one, or the first channel when they all hold the same samples. */
private fun PcmSample.toMono(): FloatArray {
    if (channels.size == 1 || channels.all { it.contentEquals(channels.first()) }) return channels.first()
    return FloatArray(frameCount) { frame -> channels.sumOf { it[frame].toDouble() }.toFloat() / channels.size }
}

/** These samples at 48 kHz, resampled by the browser from [sampleRate]. */
private suspend fun FloatArray.resampled(sampleRate: Int): FloatArray {
    if (sampleRate == OPUS_RATE || isEmpty()) return this
    val length = ceil(size.toDouble() * OPUS_RATE / sampleRate).toInt()
    val context = OfflineAudioContext(1, length, OPUS_RATE.toFloat())
    val buffer = context.createBuffer(1, size, sampleRate.toFloat())
    buffer.copyToChannel(unsafeCast<Float32Array<ArrayBuffer>>(), 0)
    val source = context.createBufferSource()
    source.buffer = buffer
    source.connect(context.destination)
    source.start()
    return context.startRendering().getChannelData(0).unsafeCast<FloatArray>()
}

private const val OPUS_RATE = 48_000
private const val OPUS_BITRATE = 96_000
private const val FRAME_SAMPLES = 960
private const val DEFAULT_PRE_SKIP = 312
private const val MICROS_PER_SECOND = 1_000_000
