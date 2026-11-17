package streetlight.web.model

import kampfire.api.toUsername
import kampfire.model.ViewModel
import kampfire.model.toDataOr
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import koala.utility.dedup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import streetlight.model.io.ApiClient
import koala.model.Toaster
import streetlight.web.view.SandboxException
import kotlin.time.Duration.Companion.seconds

class Sandbox(
    scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
): ViewModel(scope) {
    // wrapper for StateFlow
    private val state = storeOf(SandboxState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val isNameTaken = stateFlow.dedup { it.isNameTaken }
    val nameState = state.mutableTapOf({ it.name }) { copy(name = it) }
    val isBusyState = state.mutableTapOf({ it.isBusy }) { copy(isBusy = it) }

    fun checkAvailability() {
        launch(isBusyState, toaster) {
            delay(5.seconds)
            val username = stateNow.name.toUsername()
            if (username.value == "Snake") throw SandboxException()
            val isNameTaken = api.user.checkUsernameExists(username).toDataOr(toaster) {
                return@launch
            }
            val name = if (isNameTaken) "" else stateNow.name
            state.set { copy(isNameTaken = isNameTaken, name = name) }
        }
    }
}

data class SandboxState(
    val name: String = "Snake",
    val isNameTaken: Boolean? = null,
    val isBusy: Boolean = false
)


//class Foo(
//    private val scope: CoroutineScope,
//    private val api: ApiClient,
//    private val toaster: Toaster,
//) {
//    private val state = storeOf(FooState())
//
//    val nameState = state.mutableTapOf({ it.name }) { copy(name = it) }
//    val itemsState = state.tapOf { it.items }
//    val isWorkingState = state.mutableTapOf({ it.isWorking }) { copy(isWorking = it)}
//
//    init {
//        scope.launch {
//            val items = api.foo.readItems().toDataOr(toaster) { return@launch }
//            state.set { copy(items = items) }
//        }
//    }
//
//    fun addItem() {
//        val name = state.now.name.takeIf { it.isNotBlank() } ?: run {
//            toaster.deliver(Problem("A name is required."))
//            return
//        }
//        scope.launch(::addItem, isWorkingState, toaster) {
//            val item = api.foo.addItem(state.name.now).toDataOr(toaster) { return@launch }
//            state.set { copy(items = items + item) }
//        }
//    }
//}
//
//data class FooState(
//    val name: String = "",
//    val items: List<FooItem> = emptyList(),
//    val isWorking: Boolean = false,
//)
//
//data class FooItem(
//    val name: String
//)