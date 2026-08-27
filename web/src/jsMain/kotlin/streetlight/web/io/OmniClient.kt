package streetlight.web.io

import koala.model.storeOf
import koala.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.OmniHistory
import streetlight.model.data.OmniMessage
import streetlight.model.data.OmniRecord

class OmniClient(
    private val scope: CoroutineScope,
    private val api: ApiClient
) {
    private val client: SSEClient<OmniMessage> = sseClientOf(scope) { api.connectOmniLog() }

    private val state = storeOf(OmniLogState())
    val recordsState = state.tapOf { it.records }
    val lastRecordState = recordsState.tapOf { it.lastOrNull() }

    private val records = mutableListOf<OmniRecord>()

    init {
        scope.launch {
            client.messageFlow.collect {
                takeMessage(it)
            }
        }
    }

    fun connect() = client.connect()
    fun disconnect() = client.disconnect()

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

data class OmniLogState(
    val records: List<OmniRecord> = emptyList()
)

private const val MAX_RECORDS = 100