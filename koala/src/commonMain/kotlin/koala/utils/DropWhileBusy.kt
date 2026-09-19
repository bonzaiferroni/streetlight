package koala.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class DropWhileBusy {
    private var job: Job? = null
    private val isActive get() = job?.isActive == true

    fun launch(scope: CoroutineScope, name: String, block: suspend CoroutineScope.() -> Unit): Job? {
        if (isActive) return null
        return scope.launch(name, block = block).also { job = it }
    }
}
