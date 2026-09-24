package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.Outcome
import kampfire.model.toDataOr

/** A list loaded by [provisioner] on first request and kept until [clear]. */
class Fleet<T>(private val provisioner: suspend () -> Outcome<List<T>>) {
    private var fleet: List<T>? = null

    fun clear() {
        fleet = null
    }

    /** The list, loaded when not yet held, or `null` when loading fails. */
    suspend fun provisionFleet(messenger: Messenger): List<T>? =
        fleet ?: provisioner().toDataOr(messenger) { return null }.also { fleet = it }
}
