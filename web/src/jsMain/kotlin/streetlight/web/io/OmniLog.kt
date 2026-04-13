package streetlight.web.io

import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.OmniMessage
import streetlight.model.data.OmniRecord
import streetlight.model.data.OmniStatus

class OmniLog(
    private val scope: CoroutineScope,
    private val api: ApiClient
) {
    private var client: SocketClient<OmniMessage> = socketClientOf(scope) { api.connectOmniLog() }

    private val state = storeOf(OmniLogState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    private val records = mutableListOf<OmniRecord>()

    init {
        scope.launch {
            client.itemFlow.collect {
                receiveItem(it)
            }
        }
    }

    fun connect() = client.connect()
    fun disconnect() = client.disconnect()

    private fun receiveItem(message: OmniMessage) {
        when (message) {
            is OmniStatus -> {
                state.set { it.copy(starCount = message.starCount) }
            }
            is OmniRecord -> {
                receiveRecord(message)
            }
        }
    }

    private fun receiveRecord(record: OmniRecord) {
        records.add(record)
        if (records.size > MAX_RECORDS) {
            records.removeAt(0)
        }
        state.set { it.copy(records = records.toList()) }
    }
}

data class OmniLogState(
    val starCount: Int = 0,
    val records: List<OmniRecord> = emptyList()
)

private const val MAX_RECORDS = 100