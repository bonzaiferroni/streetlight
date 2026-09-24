package streetlight.web.model

import kampfire.api.Markdown
import kampfire.model.Messenger
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.toDataOr
import koala.utils.DropWhileBusy
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.BugEdit
import streetlight.model.data.Platform
import streetlight.model.ui.Screen
import streetlight.web.io.ApiClient

/** Sends the bug report being written, with the screen, path, and device it came from. */
class BugReporter(private val scope: CoroutineScope, private val api: ApiClient) {
    private val sending = DropWhileBusy()
    private val state = storeOf(BugReporterState())

    val bugState = state.mutableTapOf({ it.bug }) { copy(bug = it) }
    val descriptionState = bugState.mutableTapOf({ it.description }) { copy(description = it) }

    /** Sends the report and returns `true`, or `false` when the report is invalid or one is already sending. */
    fun report(messenger: Messenger, screen: Screen?, path: String?): Boolean {
        val edit = state.now.bug.takeIf { it.isValid }?.copy(
            screen = screen,
            path = path,
            deviceAgent = readDeviceAgent(),
        ) ?: return false
        return sending.launch(scope, ::report.name) {
            messenger.deliverSending()
            api.bug.report(edit).toDataOr(messenger, "Bug Reported.") { return@launch }
            bugState.set { copy(description = Markdown.Empty) }
        } != null
    }
}

data class BugReporterState(
    val bug: BugEdit = BugEdit(platform = Platform.Web)
)
