package kampfire.model

import kampfire.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.reflect.KClass

class ModelScope(
    private val scope: CoroutineScope,
    private val type: KClass<*>
) {
    private val jobs by lazy { mutableMapOf<String, Job>() }

    private val scopeName get() = type.simpleName ?: "ModelScope"

    fun launch(
        state: MutableTap<Boolean>? = null,
        messenger: Messenger? = null,
        message: String? = "Something went wrong.",
        block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch(scopeName, state, messenger, message) {
            block()
        }
    }

    /** [launch] that cancels the last job launched under [name], so only the newest runs. */
    fun cancelAndLaunch(
        name: String,
        messenger: Messenger? = null,
        message: String? = "Something went wrong.",
        block: suspend CoroutineScope.() -> Unit
    ) {
        jobs[name]?.cancel()
        jobs[name] = scope.launch(name, messenger = messenger, message = message, block = block)
    }
}