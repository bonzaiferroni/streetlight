package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.Outcome
import kampfire.model.toDataOr

class Fleet<T>(private val provisioner: suspend () -> Outcome<List<T>>) {
    private var fleet: List<T>? = null

    fun clear() {
        fleet = null
    }

    suspend fun provisionFleet(messenger: Messenger): List<T>? =
        fleet ?: provisioner().toDataOr(messenger) { return null }.also { fleet = it }
}
