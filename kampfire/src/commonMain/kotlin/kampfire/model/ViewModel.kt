package kampfire.model

import kampfire.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

open class ViewModel(
    protected val scope: CoroutineScope,
) {
    private val jobs by lazy { mutableMapOf<String, Job>() }

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

    /** [launch] that cancels the last job launched under [name], so only the newest runs. */
    protected fun cancelAndLaunch(
        name: String,
        messenger: Messenger? = null,
        message: String? = "Something went wrong.",
        block: suspend CoroutineScope.() -> Unit
    ) {
        jobs[name]?.cancel()
        jobs[name] = scope.launch(name, messenger = messenger, message = message, block = block)
    }
}