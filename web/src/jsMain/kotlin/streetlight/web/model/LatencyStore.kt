package streetlight.web.model

import kampfire.model.Tap
import kotlinx.coroutines.CoroutineScope
import streetlight.web.utility.localStoreOf

/**
 * The round trip from a sound scheduled on the audio clock to its arrival in a recording, as the last calibration
 * measured it, kept on the device. `null` before any calibration.
 */
class LatencyStore(scope: CoroutineScope) {
    private val store = localStoreOf<Double?>(scope, LATENCY_KEY, null)
    val latencySecondsState: Tap<Double?> = store

    fun save(latencySeconds: Double) = store.set(latencySeconds)
}

private const val LATENCY_KEY = "streetlight.latency-seconds"
