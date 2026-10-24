package streetlight.web.io

import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import streetlight.model.data.OmniHistory
import streetlight.model.io.ApiClient
import streetlight.model.data.OmniMessage
import streetlight.model.data.OmniRecord

/** The live feed of what happens across the site, keeping the latest records. */
class OmniClient(
    private val scope: CoroutineScope,
    private val api: ApiClient
) {
    private val state = storeOf(OmniLogState())
    val recordsState = state.tapOf { it.records }
    val lastRecordState = recordsState.tapOf { it.lastOrNull() }

    private val records = mutableListOf<OmniRecord>()

    private var job: Job? = null

    /** Follows the log until [disconnect]. */
    fun connect() {
        job = scope.launch {
            api.omniLog.connectOmniLog().collect { takeMessage(it) }
        }
    }

    fun disconnect() {
        job?.cancel()
        job = null
    }

    private fun takeMessage(message: OmniMessage) {
        when (message) {
            is OmniRecord -> {
                takeRecord(message)
            }
            is OmniHistory -> {
                takeHistory(message)
            }
        }
    }

    private fun takeRecord(record: OmniRecord) {
        records.add(record)
        if (records.size > MAX_RECORDS) {
            records.removeAt(0)
        }
        state.update { it.copy(records = records.toList()) }
    }

    private fun takeHistory(history: OmniHistory) {
        records.addAll(history.records)
        state.update { it.copy(records = records.toList()) }
    }
}

/** The records an [OmniClient] holds. */
data class OmniLogState(
    val records: List<OmniRecord> = emptyList()
)

private const val MAX_RECORDS = 100