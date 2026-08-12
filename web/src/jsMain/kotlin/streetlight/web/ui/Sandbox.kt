package streetlight.web.ui

import kampfire.api.toUsername
import kampfire.model.handleResponse
import koala.model.mutableTapOf
import koala.model.storeOf
import koala.model.dedup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import streetlight.web.io.ApiClient
import streetlight.web.model.Toaster
import koala.utils.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class Sandbox(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    // wrapper for StateFlow
    private val state = storeOf(SandboxState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val isNameTaken = stateFlow.dedup { it.isNameTaken }

    val nameField = state.mutableTapOf({ it.name }) { copy(name = it) }

    fun checkAvailability() {
        scope.launch(::checkAvailability.name, toaster, "arrr ${Random.nextInt()}") {
            delay(5.seconds)
            println(null.asDynamic().anything)
            val isNameTaken = api.checkUsernameExists(stateNow.name.toUsername()).handleResponse(toaster)
            val name = if (isNameTaken == true) "" else stateNow.name
            state.setValue { it.copy(isNameTaken = isNameTaken, name = name) }
        }
    }
}

data class SandboxState(
    val name: String = "",
    val isNameTaken: Boolean? = null,
)

