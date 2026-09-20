package streetlight.web.model

import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.BugEdit
import streetlight.model.data.Platform
import streetlight.web.io.ApiClient

class BugReporter(private val scope: CoroutineScope, private val api: ApiClient) {
    private val state = storeOf(BugReporterState())

    val bugState = state.mutableTapOf({ it.bug }) { copy(bug = it) }

    fun report() {

    }
}

data class BugReporterState(
    val bug: BugEdit = BugEdit(platform = Platform.Web)
)