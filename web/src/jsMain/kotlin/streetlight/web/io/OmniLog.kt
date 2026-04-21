package streetlight.web.io

import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.OmniHistory
import streetlight.model.data.OmniMessage
import streetlight.model.data.OmniRecord
import streetlight.model.data.OmniStatus

class OmniLog(
    private val scope: CoroutineScope,
    private val api: ApiClient
) {
    private val client: SocketClient<OmniMessage> = socketClientOf(scope) { api.connectOmniLog() }

    private val state = storeOf(OmniLogState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    private val records = mutableListOf<OmniRecord>()

    init {
        scope.launch {
            client.itemFlow.collect {
                takeMessage(it)
            }
        }
    }

    fun connect() = client.connect()
    fun disconnect() = client.disconnect()

    private fun takeMessage(message: OmniMessage) {
        when (message) {
            is OmniStatus -> {
                state.set { it.copy(starCount = message.starCount) }
            }
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
        state.set { it.copy(records = records.toList()) }
    }

    private fun takeHistory(history: OmniHistory) {
        state.set { it.copy(records = history.records) }
    }
}

data class OmniLogState(
    val starCount: Int = 0,
    val records: List<OmniRecord> = emptyList()
)

private const val MAX_RECORDS = 100