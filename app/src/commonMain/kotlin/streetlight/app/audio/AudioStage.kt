package streetlight.app.audio

import kotlinx.coroutines.coroutineScope
import kotlin.math.tanh

class AudioStage(
    private val sampleSize: Int = 44_100,
) {
    private val layers = mutableListOf<AudioLayer>()
    private var position = 0
    private var isPlaying = false

    private val layerSample = ShortArray(sampleSize)
    private val stage = ShortArray(sampleSize)
    private val liveSample = ShortArray(sampleSize)

    fun add(layer: AudioLayer) {
        layers.add(layer)
    }

    suspend fun play(
        listener: (ShortArray) -> Unit
    ) = coroutineScope {
        isPlaying = true
        position = 0

        while (isPlaying) {
            println("generating $position")
            stage.fill(0)
            var hasContent = false
            layers.forEach { layer ->
                if (layer.hasContent(position)) {
                    hasContent = true
                    layerSample.fill(0)
                    layer.sampleAt(position, layerSample)
                    stage.addSample(layerSample)
                }
            }
            if (hasContent) {
                position += sampleSize
            } else {
                isPlaying = false
            }

            if (position > 0) {
                stage.copyInto(liveSample)
                println("playing $position")
                listener(liveSample)
            }
        }

        println("finished")
    }
}

private fun addShortsWithClip(a: Short, b: Short) = (a + b).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

private fun addShortsWithSoftClip(a: Short, b: Short): Short {
    val sum = a.toInt() + b.toInt()
    val norm = sum / 32768.0
    val clipped = tanh(norm)
    return (clipped * 32767).toInt().toShort()
}

interface AudioLayer {
    fun hasContent(position: Int): Boolean
    fun sampleAt(position: Int, buffer: ShortArray)
}

class PcmLayer(
    private val pcm: ShortArray,
): AudioLayer {

    override fun hasContent(position: Int) = position < pcm.size

    override fun sampleAt(position: Int, buffer: ShortArray) {
        val len = buffer.size
        val available = (pcm.size - position).coerceAtLeast(0)
        val toCopy = len.coerceAtMost(available)
        if (toCopy > 0) {
            pcm.copyInto(buffer, 0, position, position + toCopy)
        }
        if (toCopy < len) {
            // zero fill remainder
            buffer.fill(0, toCopy, len)
        }
    }
}

fun ShortArray.addSample(
    sample: ShortArray,
    position: Int = 0,
    samplePosition: Int = 0
) {
    if (position >= size || samplePosition >= sample.size) return

    val maxCount = minOf(size - position, sample.size - samplePosition)
    for (i in 0 until maxCount) {
        val dstIndex = position + i
        val srcIndex = samplePosition + i
        this[dstIndex] = addShortsWithSoftClip(this[dstIndex], sample[srcIndex])
    }
}