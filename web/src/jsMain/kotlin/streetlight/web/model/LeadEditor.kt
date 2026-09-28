package streetlight.web.model

import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.toDataOr
import kampfire.model.toUrl
import koala.dom.MessageStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.LeadType
import streetlight.model.data.StarLead
import streetlight.web.io.ApiClient

/** The lead field of a scout: a url of [leadType] sent for the crawler to read, from [galaxy] when there is one. */
class LeadEditor(
    val leadType: LeadType,
    private val galaxy: Galaxy?,
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val state = storeOf(LeadEditorState())
    val stateNow get() = state.now
    val stateFlow = state.flow
    val message = MessageStore()

    val urlState = state.mutableTapOf({ it.url }) { copy(url = it) }

    /** Sends the url typed in as a lead for the crawler to read. */
    fun submit() {
        val text = stateNow.url.trim()
        if (text.isBlank()) return
        val url = text.toUrl().takeIf { it.isAbsolute } ?: run {
            message.deliver("That isn't a web address.")
            return
        }
        message.deliverSending("Sending...")
        scope.launch {
            api.star.createLead(StarLead(url, leadType, galaxy?.galaxyId)).toDataOr(message) { return@launch }
            message.deliverSuccess("Checking it out.")
            state.set { copy(url = "") }
        }
    }

    /** Clears the url and its message. */
    fun reset() {
        state.set { LeadEditorState() }
        message.clear()
    }
}

data class LeadEditorState(
    val url: String = "",
)
