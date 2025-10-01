package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.format
import kotlinx.coroutines.launch
import pondui.ui.controls.Button
import pondui.ui.controls.Section
import pondui.ui.services.AudioSpec
import pondui.ui.services.MicPermissionRequester
import pondui.ui.services.MicStream
import pondui.ui.services.createMicStream
import kotlin.math.pow

@Composable
fun PitchStreamView(
    viewModel: PitchStreamModel = viewModel { PitchStreamModel() }
) {
    Section {
        MicPermissionRequester {
            Button("Start", onClick = viewModel::start)
            Button("Stop", onClick = viewModel::stop)
        }
    }
}

class PitchStreamModel: ViewModel() {

    var stream: MicStream? = null

    fun start() {
        viewModelScope.launch {
            val frames = 4000
            val channels = 1
            val sampleRate = 16000
            stream = createMicStream(
                AudioSpec(sampleRate = sampleRate, channels = channels, framesPerChunk = frames)
            ) { pcm, frames ->
                // pcm: ShortArray fromDegree size frames * channels (S16LE)
                // Do yer DSP, meter, encoder, or socket send here.
                val volume = averageVolume(pcm, frames, channels).toFloat()
                val pitch = pitchHzFromPcmShorts(pcm, sampleRate)
                val note = noteFromFreq(pitch)
                println("vol: ${volume.format(2)} note: ${note?.name}")
            }
            stream?.start()
        }
    }

    fun stop() {
        viewModelScope.launch {
            stream?.stop()
        }
    }
}

fun averageVolume(pcm: ShortArray, frames: Int, channels: Int): Double {
    var sumSq = 0.0
    val samples = frames * channels
    for (i in 0 until samples) {
        val s = pcm[i].toDouble() / Short.MAX_VALUE // normalize -1..1
        sumSq += s * s
    }
    val rms = kotlin.math.sqrt(sumSq / samples)
    return rms // 0.0..1.0 (average power)
}

fun rmsToDb(rms: Double): Double {
    return 20 * kotlin.math.log10(rms.coerceAtLeast(1e-9))
}

fun pitchHzFromPcmShorts(
    pcm: ShortArray,
    sampleRate: Int,
    length: Int = minOf(2048, pcm.size)
): Double {
    if (length < 512) return Double.NaN
    val samples = DoubleArray(length) { i ->
        val w = 0.54 - 0.46 * kotlin.math.cos(2.0 * Math.PI * i / (length - 1))
        (pcm[i] / 32768.0) * w
    }
    // quick silence gate
    val rms = kotlin.math.sqrt(samples.sumOf { it * it } / length)
    if (rms < 0.01) return Double.NaN

    val minF = 50.0
    val maxF = 1000.0
    val minLag = (sampleRate / maxF).toInt()
    val maxLag = (sampleRate / minF).toInt()

    var bestLag = -1
    var bestVal = Double.NEGATIVE_INFINITY
    for (lag in minLag..maxLag) {
        var score = 0.0
        var i = 0
        val end = length - lag
        while (i < end) { score += samples[i] * samples[i + lag]; i++ }
        if (score > bestVal) { bestVal = score; bestLag = lag }
    }
    return if (bestLag > 0) sampleRate.toDouble() / bestLag else Double.NaN
}

data class NoteGuess(val name: String, val octave: Int, val cents: Int, val freq: Double)

fun noteFromFreq(f: Double): NoteGuess? {
    if (!f.isFinite() || f <= 0.0) return null
    val n = kotlin.math.round(12.0 * kotlin.math.log(f / 440.0, 2.0)).toInt()
    val noteNames = arrayOf("C","C#","D","D#","E","F","F#","G","G#","A","A#","B")
    val idx = Math.floorMod(n + 9, 12) // align so C=0
    val octave = 4 + (n + 9) / 12
    val ref = 440.0 * 2.0.pow(n / 12.0)
    val cents = kotlin.math.round(1200.0 * kotlin.math.log(f / ref, 2.0)).toInt()
    return NoteGuess(noteNames[idx], octave, cents, f)
}

private fun log(x: Double, base: Double) = kotlin.math.ln(x) / kotlin.math.ln(base)