package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Tap
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Measures the setup's [AcousticProfile] by playing a sine sweep through the speakers on [player] and recording it on
 * [recorder], and keeps its round-trip latency in [latencyStore] for every later take.
 */
class Calibrator(
    private val audio: StudioAudio,
    private val recorder: AudioRecorder,
    private val player: AudioPlayer,
    private val latencyStore: LatencyStore,
) {
    private val calibratingStore = storeOf(false)
    val isCalibratingState: Tap<Boolean> = calibratingStore

    /** Whether a calibration has measured the setup's latency. */
    val isCalibratedState = latencyStore.latencySecondsState.tapOf { it != null }

    /**
     * Captures a lead-in of silence, then the sweep and the tail that follows it, and measures the profile from them,
     * or gives the problem, as when the mic never heard the sweep. Does nothing while a recording or calibration runs.
     */
    suspend fun calibrate(): Outcome<AcousticProfile>? {
        if (calibratingStore.now || recorder.isRecordingState.now) return null
        calibratingStore.set(true)
        return try {
            recorder.start()
            delay(LEAD_IN_MILLIS)
            val sweep = sineSweepOf(audio.sampleRate)
            val sweepClock = player.play(listOf(PlacedPcm(sweep, 0.0)), 0.0)
            delay((sweep.durationSeconds * 1000).toLong() + TAIL_MILLIS)
            val response = recorder.stop() ?: return null
            val profile = measureAcousticProfile(sweep, sweepClock, response)
            if (profile.arrivalDb < MIN_ARRIVAL_DB) {
                return Problem("The mic was unable to detect the calibration sweep.")
            }
            latencyStore.save(profile.latencySeconds)
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

private const val LEAD_IN_MILLIS = 1000L
private const val TAIL_MILLIS = 1000L

// noise alone peaks near 13 dB over a second of lags
private const val MIN_ARRIVAL_DB = 20.0
