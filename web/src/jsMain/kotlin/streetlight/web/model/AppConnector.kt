package streetlight.web.model

import kampfire.model.mutableTapOf
import kampfire.model.storeOf

class AppConnector {
    private val state = storeOf(AppConnectorState())

    val paypalIdState = state.mutableTapOf({ it.paypalId }) { copy(paypalId = it.trim()) }
    val venmoIdState = state.mutableTapOf({ it.venmoId }) { copy(venmoId = it.trim()) }
    val cashAppIdState = state.mutableTapOf({ it.cashAppId }) { copy(cashAppId = it.trim()) }
}

data class AppConnectorState(
    val paypalId: String = "",
    val venmoId: String = "",
    val cashAppId: String = ""
)