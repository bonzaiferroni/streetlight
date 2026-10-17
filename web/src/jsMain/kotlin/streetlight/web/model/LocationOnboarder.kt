package streetlight.web.model

import kampfire.model.mutableTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import koala.model.MessageStore
import koala.utility.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import streetlight.model.data.GatherEventStatus
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.ParseConfig
import streetlight.model.data.LocationEdit
import streetlight.model.data.toEdit
import streetlight.model.data.toEditOrNull
import streetlight.web.io.ApiClient
import streetlight.web.io.OSMClient

/** The onboarding of a location's owner, finding their location in Streetlight or in OpenStreetMap, then editing it. */
class LocationOnboarder(
    val editor: LocationEditor,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val osm: OSMClient,
) {
    private val state = storeOf(LocationOnboarderState())
    private val stateNow get() = state.now
    val searchMessage = MessageStore()
    val focusMessage = MessageStore()
    private var searchJob: Job? = null

    val resultsState = state.tapOf { it.results }
    val isOSMState = state.tapOf { it.isOSM }
    val focusState = state.tapOf { it.focus }
    val selectionState = state.mutableTapOf({ it.focus as? LocationEdit }) { copy(focus = it) }

    init {
        scope.launch("read hosted configs") {
            val hosted = api.location.readHostedConfigs().toDataOrNull(searchMessage).orEmpty()
            state.set { copy(focus = if (hosted.isEmpty()) NullLocation else HostedLocations(hosted)) }
        }

        focusState.reactIn(scope) { focus ->
            if (focus is LocationEdit) editor.editField.set(focus)
        }
    }

    /** Looks for the location named [name] at [address] in Streetlight, in place of any search still running. */
    fun searchLocations(name: String, address: String) {
        searchJob?.cancel()
        searchMessage.clear()
        searchJob = scope.launch(::searchLocations) {
            val matches = when {
                name.isBlank() && address.isBlank() -> emptyList()
                else -> api.location.matchLocations(name, address.takeIf { it.isNotBlank() })
                    .toDataOr(searchMessage) { return@launch }
            }
            state.set { copy(results = matches.map { it.toEdit() }, isOSM = false) }
        }
    }

    /** Picks up the onboarding of a hosted location: its design when it has none, otherwise how its events are read. */
    fun resumeHosted(content: LocationConfigContent) {
        if (content.config.design == null) {
            state.set { copy(focus = content) }
            return
        }
        scope.launch(::resumeHosted) {
            focusParseMode(content, focusMessage)
        }
    }

    /** Starts the search for a location to onboard. */
    fun addNewLocation() = state.set { copy(focus = NullLocation) }

    /** Saves the edited location and makes its config the focus. */
    fun submitLocation() {
        scope.launch(::submitLocation) {
            val location = editor.submitSuspend() ?: return@launch
            val content = api.location.readLocationConfigContent(location.locationId)
                .toDataOr(editor.messages) { return@launch }
            state.set { copy(focus = content) }
        }
    }

    /** Saves the design built by [designer] to the focused config, then moves on to how its events are read. */
    fun submitDesign(designer: DesignEditor, messages: MessageStore) {
        val content = stateNow.focus as? LocationConfigContent ?: return
        scope.launch(::submitDesign) {
            val design = designer.build(messages)
            messages.deliverSending()
            api.location.updateLocationConfig(content.config.copy(design = design)).toDataOr(messages) { return@launch }
            val updated = api.location.readLocationConfigContent(content.location.locationId)
                .toDataOr(messages) { return@launch }
            focusParseMode(updated, messages)
        }
    }

    /** Makes how the events of [content] are read the focus, with the status the crawler reports. */
    private suspend fun focusParseMode(content: LocationConfigContent, messages: MessageStore) {
        val status = api.location.readGatherStatus(content.location.locationId).toDataOr(messages) { return }
        state.set { copy(focus = ParseModeFocus(content, status)) }
    }

    /** Saves [config] when it changes the location's, then advances. */
    fun submitParseConfig(config: ParseConfig, messages: MessageStore) {
        val content = (stateNow.focus as? ParseModeFocus)?.content ?: return
        if (config.eventsUrl == content.location.eventsUrl && config.parseMode == content.config.parseMode) {
            state.set { copy(focus = FocusTBD) }
            return
        }
        scope.launch(::submitParseConfig) {
            messages.deliverSending()
            api.location.updateParseConfig(config).toDataOr(messages) { return@launch }
            state.set { copy(focus = FocusTBD) }
        }
    }

    /** Searches OpenStreetMap for [name] at [address], in place of any search still running. */
    fun searchOSM(name: String, address: String) {
        searchJob?.cancel()
        searchMessage.deliverSending("Searching OpenStreetMap...")
        searchJob = scope.launch(::searchOSM) {
            val query = listOf(name, address).filter { it.isNotBlank() }.joinToString(", ")
            val places = osm.readLocations(query).toDataOr(searchMessage) { return@launch }
                .mapNotNull { it.toEditOrNull() }
            if (places.isEmpty()) searchMessage.deliver("No locations found.") else searchMessage.clear()
            state.set { copy(results = places, isOSM = true) }
        }
    }
}

data class LocationOnboarderState(
    val results: List<LocationEdit> = emptyList(),
    val isOSM: Boolean = false,
    val focus: Any? = null,
)

/** The focus of a search for a location to onboard, before one is chosen. */
object NullLocation

/** The focus of a choice among the [locations] the star hosts. */
data class HostedLocations(val locations: List<LocationConfigContent>)

/** The focus of how the crawler reads the events of a designed location, with its [status]. */
data class ParseModeFocus(
    val content: LocationConfigContent,
    val status: GatherEventStatus,
)

/** The focus that follows the parse mode, still to be decided. */
object FocusTBD