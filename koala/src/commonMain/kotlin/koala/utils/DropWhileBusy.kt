package koala.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/** Launches a block only when its previous launch has finished. A launch requested while one is active is dropped. */
class DropWhileBusy {
    private var job: Job? = null
    private val isActive get() = job?.isActive == true

    /** Launches [block] in [scope], or returns `null` without launching while the previous launch is active. */
    fun launch(scope: CoroutineScope, name: String, block: suspend CoroutineScope.() -> Unit): Job? {
        if (isActive) return null
        return scope.launch(name, block = block).also { job = it }
    }
}
