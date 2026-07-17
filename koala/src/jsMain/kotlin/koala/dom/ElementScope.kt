package koala.dom

import koala.core.ScopeTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import org.w3c.dom.Element

internal val Element.job: Job? get() = asDynamic().job

internal fun Element.setJob(value: Job?) {
    val element = asDynamic()
    val existingJob = element.job
    if (existingJob != null && existingJob.isActive) {
        error("Active coroutine job cannot be replaced")
    }
    element.job = value
}

internal val Element.scope: CoroutineScope? get() = asDynamic().scope

internal fun Element.setScope(value: CoroutineScope?) {
    val element = asDynamic()
    val existingScope = this@setScope.scope
    if (existingScope != null && existingScope.isActive) {
        error("Active coroutine scope cannot be replaced")
    }
    element.scope = value
}

internal fun Element.clearScope() {
    this.job?.cancel()
    setJob(null)
    setScope(null)
}

internal fun Element.provisionScope(
    name: String,
    parentScope: CoroutineScope,
    cancelExistingScope: Boolean
): CoroutineScope {
    if (cancelExistingScope) {
        this.job?.cancel()
    }
    val scope = this@provisionScope.scope
    if (scope != null && scope.isActive) {
        return scope
    }

    val job = SupervisorJob(parentScope.coroutineContext[Job])
    val telemetry = ScopeTelemetry(name, this, parentScope.coroutineContext[ScopeTelemetry])
    val newScope = CoroutineScope(parentScope.coroutineContext + job + telemetry)
    setJob(job)
    setScope(newScope)
    return newScope
}

fun Element.queryScope(): CoroutineScope? = this@queryScope.scope ?: parentElement?.queryScope()
