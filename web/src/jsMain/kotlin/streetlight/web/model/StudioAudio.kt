package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Tap
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import streetlight.web.utility.localStoreOf
import web.audio.AudioContext

/**
 * The [AudioContext] the studio shares, so recording and playback run on one clock, with the mic and player on it
 * and the round trip a calibration measured, kept on the device through [scope].
 */
class StudioAudio(scope: CoroutineScope) {
    val context = AudioContext()

    val recorder = AudioRecorder(::startContext)
    val player = AudioPlayer(::startContext)

    private val latencyStore = localStoreOf<Double?>(scope, LATENCY_KEY, null)
    private val calibratingStore = storeOf(false)

    /** The audio clock in seconds, which playback and recording are scheduled against. */
    val clockSeconds get() = context.currentTime

    /** The sample rate the shared context runs at. */
    val sampleRate get() = context.sampleRate.toInt()

    /** The round trip from a sound scheduled on the clock to its arrival in a recording, or `null` before a calibration. */
    val latencySecondsState: Tap<Double?> = latencyStore

    /** Whether a calibration has measured the latency. */
    val isCalibratedState = latencyStore.tapOf { it != null }

    val isCalibratingState: Tap<Boolean> = calibratingStore

    /**
     * The shared context, resumed for a recording or playback about to start. A browser suspends a context made
     * before the user's first gesture, and resuming one that runs does nothing.
     */
    fun startContext(): AudioContext {
        context.resumeAsync()
        return context
    }

    /**
     * Measures the setup's [AcousticProfile] from a sine sweep played through the speakers and keeps its latency, or
     * gives the problem, as when the mic never heard the sweep. Does nothing while a recording or calibration runs.
     */
    suspend fun calibrate(): Outcome<AcousticProfile>? {
        if (calibratingStore.now || recorder.isRecordingState.now) return null
        calibratingStore.set(true)
        return try {
            recorder.start()
            delay(LEAD_IN_MILLIS)
            val sweep = sineSweepOf(sampleRate)
            val sweepClock = player.play(listOf(PlacedPcm(sweep, 0.0)), 0.0)
            delay((sweep.durationSeconds * 1000).toLong() + TAIL_MILLIS)
            val response = recorder.stop() ?: return null
            val profile = measureAcousticProfile(sweep, sweepClock, response)
            if (profile.arrivalDb < MIN_ARRIVAL_DB) {
                return Problem("The mic was unable to detect the calibration sweep.")
            }
            latencyStore.set(profile.latencySeconds)
            Ok(profile)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Problem("Calibration failed${error.message?.let { ": $it" } ?: ""}")
        } finally {
            recorder.cancel()
            player.stop()
            calibratingStore.set(false)
        }
    }
}

private const val LATENCY_KEY = "streetlight.latency-seconds"
private const val LEAD_IN_MILLIS = 1000L
private const val TAIL_MILLIS = 1000L

// noise alone peaks near 13 dB over a second of lags
private const val MIN_ARRIVAL_DB = 20.0
