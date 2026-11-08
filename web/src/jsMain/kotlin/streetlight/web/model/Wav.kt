package streetlight.web.model

import kotlin.math.roundToInt

/**
 * Writes this sample as a 16-bit PCM WAV file. Channels that all hold the same samples, as a mono mic delivered in
 * stereo, are written as one.
 */
fun PcmSample.toWav(): ByteArray {
    val written = if (channels.all { it.contentEquals(channels.first()) }) channels.take(1) else channels
    val channelCount = written.size
    val dataSize = frameCount * channelCount * BYTES_PER_SAMPLE
    val bytes = ByteArray(HEADER_SIZE + dataSize)

    // write header
    bytes.putAscii(0, "RIFF")
    bytes.putInt(4, HEADER_SIZE - 8 + dataSize)
    bytes.putAscii(8, "WAVE")
    bytes.putAscii(12, "fmt ")
    bytes.putInt(16, 16)
    bytes.putShort(20, PCM_FORMAT)
    bytes.putShort(22, channelCount)
    bytes.putInt(24, sampleRate)
    bytes.putInt(28, sampleRate * channelCount * BYTES_PER_SAMPLE)
    bytes.putShort(32, channelCount * BYTES_PER_SAMPLE)
    bytes.putShort(34, BYTES_PER_SAMPLE * 8)
    bytes.putAscii(36, "data")
    bytes.putInt(40, dataSize)

    // interleave samples
    var offset = HEADER_SIZE
    for (frame in 0 until frameCount) {
        for (channel in written) {
            bytes.putShort(offset, (channel[frame].coerceIn(-1f, 1f) * Short.MAX_VALUE).roundToInt())
            offset += BYTES_PER_SAMPLE
        }
    }
    return bytes
}

/** Reads [bytes] as a 16-bit PCM WAV file, or `null` when they hold another format. */
fun readWav(bytes: ByteArray): PcmSample? {
    if (bytes.size < 12 || bytes.readAscii(0) != "RIFF" || bytes.readAscii(8) != "WAVE") return null

    // find the format and data chunks
    var offset = 12
    var channelCount = 0
    var sampleRate = 0
    var isReadable = false
    while (offset + 8 <= bytes.size) {
        val id = bytes.readAscii(offset)
        val size = bytes.readInt(offset + 4)
        val body = offset + 8
        when (id) {
            "fmt " -> {
                channelCount = bytes.readShort(body + 2)
                sampleRate = bytes.readInt(body + 4)
                isReadable = bytes.readShort(body) == PCM_FORMAT && bytes.readShort(body + 14) == BYTES_PER_SAMPLE * 8
            }
            "data" -> {
                if (!isReadable || channelCount == 0) return null
                val frameCount = minOf(size, bytes.size - body) / (channelCount * BYTES_PER_SAMPLE)
                val channels = List(channelCount) { FloatArray(frameCount) }
                var sampleOffset = body
                for (frame in 0 until frameCount) {
                    channels.forEach { channel ->
                        channel[frame] = bytes.readShort(sampleOffset).toShort().toFloat() / Short.MAX_VALUE
                        sampleOffset += BYTES_PER_SAMPLE
                    }
                }
                return PcmSample(sampleRate, channels)
            }
        }
        // chunks are padded to an even size
        offset = body + size + (size and 1)
    }
    return null
}

private fun ByteArray.putAscii(offset: Int, text: String) = text.forEachIndexed { index, char ->
    this[offset + index] = char.code.toByte()
}

private fun ByteArray.putShort(offset: Int, value: Int) {
    this[offset] = value.toByte()
    this[offset + 1] = (value shr 8).toByte()
}

private fun ByteArray.putInt(offset: Int, value: Int) {
    putShort(offset, value)
    putShort(offset + 2, value shr 16)
}

private fun ByteArray.readAscii(offset: Int) = (0 until 4).joinToString("") { this[offset + it].toInt().toChar().toString() }

private fun ByteArray.readShort(offset: Int) = (this[offset].toInt() and 0xFF) or ((this[offset + 1].toInt() and 0xFF) shl 8)

private fun ByteArray.readInt(offset: Int) = readShort(offset) or (readShort(offset + 2) shl 16)

private const val HEADER_SIZE = 44
private const val BYTES_PER_SAMPLE = 2
private const val PCM_FORMAT = 1
