package kampfire.model

import kampfire.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class ViewModel(
    protected val scope: CoroutineScope,
) {
    protected fun launch(
        state: MutableTap<Boolean>? = null,
        messenger: Messenger? = null,
        message: String? = "Something went wrong.",
        block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch(this::class, state, messenger, message) {
            block()
        }
    }
}