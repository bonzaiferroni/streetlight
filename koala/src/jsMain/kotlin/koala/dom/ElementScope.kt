package koala.dom

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import org.w3c.dom.Element

private var Element.job: Job? get() = asDynamic().job
    set(value) {
        val element = asDynamic()
        val existingJob = element.job
        if (existingJob != null && existingJob.isActive) {
            error("Active coroutine job cannot be replaced")
        }
        element.job = value
    }

private var Element.scope: CoroutineScope? get() = asDynamic().scope
    set(value) {
        val element = asDynamic()
        val existingScope = scope
        if (existingScope != null && existingScope.isActive) {
            error("Active coroutine scope cannot be replaced")
        }
        element.scope = value
    }

fun Element.clearScope() {
    this.job?.cancel()
    this.job = null
    this.scope = null
}

fun Element.getScope(parentScope: CoroutineScope, cancelExistingScope: Boolean): CoroutineScope {
    if (cancelExistingScope) {
        this.job?.cancel()
    }
    val scope = scope
    if (scope != null && scope.isActive) {
        return scope
    }

    val job = SupervisorJob()
    this.job = job
    val newScope = CoroutineScope(parentScope.coroutineContext + job)
    this.scope = newScope
    return newScope
}

fun Element.queryScope(): CoroutineScope? = scope ?: parentElement?.queryScope()
fun Element.queryJob(): Job? = job ?: parentElement?.queryJob()