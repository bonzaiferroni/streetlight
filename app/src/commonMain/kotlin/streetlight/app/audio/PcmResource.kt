package streetlight.app.audio

import org.jetbrains.compose.resources.ExperimentalResourceApi
import streetlight.app.generated.resources.Res
import kotlin.math.floor
import kotlin.math.roundToInt

class WavPcm16(
    val sampleRate: Int,
    val channels: Int,
    val pcm: ShortArray // interleaved if channels > 1
)

@OptIn(ExperimentalResourceApi::class)
suspend fun loadPcmFromWav(
    resourcePath: String,      // e.g. "files/audio/my_sound.wav"
    targetSampleRate: Int,
    targetChannels: Int        // 1 or 2
): ShortArray {
    require(targetChannels == 1 || targetChannels == 2) {
        "Target channels must be 1 or 2, matey."
    }

    val bytes = Res.readBytes(resourcePath)
    val wav = parseWavPcm16(bytes)

    if (wav.channels == 1 && targetChannels == 2) {
        error("Mono → stereo not supported, ye scallywag.")
    }

    val convertedChannelsPcm: ShortArray =
        when {
            wav.channels == targetChannels -> wav.pcm
            wav.channels == 2 && targetChannels == 1 -> stereoToMono(wav.pcm)
            else -> error("Unsupported channel conversion: ${wav.channels} → $targetChannels")
        }

    return if (wav.sampleRate == targetSampleRate) {
        convertedChannelsPcm
    } else {
        resamplePcm16(
            src = convertedChannelsPcm,
            srcSampleRate = wav.sampleRate,
            dstSampleRate = targetSampleRate,
            channels = targetChannels
        )
    }
}

// -------- WAV parsing (RIFF / fmt / data) --------

private fun parseWavPcm16(bytes: ByteArray): WavPcm16 {
    require(bytes.size >= 12 &&
            bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'A'.code.toByte() &&
            bytes[10] == 'V'.code.toByte() &&
            bytes[11] == 'E'.code.toByte()
    ) { "Not a RIFF/WAVE file, matey." }

    var offset = 12

    var audioFormat = -1
    var numChannels = -1
    var sampleRate = -1
    var bitsPerSample = -1
    var dataOffset = -1
    var dataSize = -1

    while (offset + 8 <= bytes.size) {
        val id0 = bytes[offset].toInt().toChar()
        val id1 = bytes[offset + 1].toInt().toChar()
        val id2 = bytes[offset + 2].toInt().toChar()
        val id3 = bytes[offset + 3].toInt().toChar()
        val size = readUInt32LE(bytes, offset + 4)

        when {
            id0 == 'f' && id1 == 'm' && id2 == 't' && id3 == ' ' -> {
                audioFormat = readUInt16LE(bytes, offset + 8)
                numChannels = readUInt16LE(bytes, offset + 10)
                sampleRate = readUInt32LE(bytes, offset + 12)
                bitsPerSample = readUInt16LE(bytes, offset + 22)
            }
            id0 == 'd' && id1 == 'a' && id2 == 't' && id3 == 'a' -> {
                dataOffset = offset + 8
                dataSize = size
            }
        }

        val skip = 8 + size
        offset += skip + (skip % 2) // chunks are word-aligned
    }

    require(audioFormat == 1) { "Only PCM (audioFormat=1) WAV supported, got $audioFormat." }
    require(bitsPerSample == 16) { "Only 16-bit WAV supported, got $bitsPerSample bits." }
    require(numChannels > 0) { "Invalid channel count: $numChannels." }
    require(dataOffset >= 0 && dataSize > 0) { "No data chunk found in WAV." }

    val bytesPerSample = bitsPerSample / 8
    val frameSize = bytesPerSample * numChannels
    val totalFrames = dataSize / frameSize
    val totalSamples = totalFrames * numChannels

    val pcm = ShortArray(totalSamples)
    var srcIndex = dataOffset
    var dstIndex = 0

    repeat(totalSamples) {
        val lo = bytes[srcIndex].toInt() and 0xFF
        val hi = bytes[srcIndex + 1].toInt()
        pcm[dstIndex] = ((hi shl 8) or lo).toShort()
        srcIndex += 2
        dstIndex += 1
    }

    return WavPcm16(
        sampleRate = sampleRate,
        channels = numChannels,
        pcm = pcm
    )
}

private fun readUInt16LE(bytes: ByteArray, offset: Int): Int {
    val b0 = bytes[offset].toInt() and 0xFF
    val b1 = bytes[offset + 1].toInt() and 0xFF
    return b0 or (b1 shl 8)
}

private fun readUInt32LE(bytes: ByteArray, offset: Int): Int {
    val b0 = bytes[offset].toInt() and 0xFF
    val b1 = bytes[offset + 1].toInt() and 0xFF
    val b2 = bytes[offset + 2].toInt() and 0xFF
    val b3 = bytes[offset + 3].toInt() and 0xFF
    return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
}

// -------- Channel conversion --------

private fun stereoToMono(stereo: ShortArray): ShortArray {
    require(stereo.size % 2 == 0) { "Stereo PCM must have even length." }
    val frames = stereo.size / 2
    val mono = ShortArray(frames)
    var i = 0
    var j = 0
    while (i < frames) {
        val l = stereo[j].toInt()
        val r = stereo[j + 1].toInt()
        mono[i] = ((l + r) / 2).toShort()
        i += 1
        j += 2
    }
    return mono
}

// -------- Resampling (linear interpolation) --------

private fun resamplePcm16(
    src: ShortArray,
    srcSampleRate: Int,
    dstSampleRate: Int,
    channels: Int
): ShortArray {
    return if (channels == 1) {
        resampleMono(src, srcSampleRate, dstSampleRate)
    } else {
        resampleInterleaved(src, srcSampleRate, dstSampleRate, channels)
    }
}

private fun resampleMono(
    src: ShortArray,
    srcSampleRate: Int,
    dstSampleRate: Int
): ShortArray {
    if (src.isEmpty() || srcSampleRate == dstSampleRate) return src

    val dstLength = (src.size.toLong() * dstSampleRate / srcSampleRate).toInt()
    val dst = ShortArray(dstLength)

    val rateRatio = srcSampleRate.toDouble() / dstSampleRate.toDouble()
    val lastIndex = src.size - 1

    for (i in 0 until dstLength) {
        val srcPos = i * rateRatio
        val idx = floor(srcPos).toInt()
        val frac = srcPos - idx

        val s1 = src[idx].toInt()
        val s2 = src[minOf(idx + 1, lastIndex)].toInt()

        val sample = (s1 * (1.0 - frac) + s2 * frac).roundToInt()
        dst[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }

    return dst
}

private fun resampleInterleaved(
    src: ShortArray,
    srcSampleRate: Int,
    dstSampleRate: Int,
    channels: Int
): ShortArray {
    if (src.isEmpty() || srcSampleRate == dstSampleRate) return src

    val srcFrames = src.size / channels
    val dstFrames = (srcFrames.toLong() * dstSampleRate / srcSampleRate).toInt()
    val dst = ShortArray(dstFrames * channels)

    val frameRatio = srcFrames.toDouble() / dstFrames.toDouble()
    val lastFrame = srcFrames - 1

    var dstIndex = 0
    for (i in 0 until dstFrames) {
        val srcPos = i * frameRatio
        val frameIdx = floor(srcPos).toInt()
        val frac = srcPos - frameIdx
        val nextFrame = minOf(frameIdx + 1, lastFrame)

        val base0 = frameIdx * channels
        val base1 = nextFrame * channels

        for (c in 0 until channels) {
            val s1 = src[base0 + c].toInt()
            val s2 = src[base1 + c].toInt()
            val sample = (s1 * (1.0 - frac) + s2 * frac).roundToInt()
            dst[dstIndex + c] =
                sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        dstIndex += channels
    }

    return dst
}
