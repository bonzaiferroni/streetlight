package streetlight.web.io

import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.EventCreated
import streetlight.model.data.EventEdited
import streetlight.model.data.OmniMessage
import streetlight.model.data.OmniStatus

class OmniLog(
    private val scope: CoroutineScope,
    private val api: ApiClient
) {
    private var client: SocketClient<OmniMessage> = socketClientOf(scope) { api.connectOmniLog() }

    private val state = storeOf(OmniLogState())
    val stateFlow = state.flow
    val stateNow get() = state.now

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
            is EventCreated -> TODO()
            is EventEdited -> TODO()
            is OmniStatus -> {
                state.set { it.copy(starCount = message.starCount) }
                console.log("starCount: ${message.starCount}")
            }
        }
    }

}

data class OmniLogState(
    val starCount: Int = 0,
)