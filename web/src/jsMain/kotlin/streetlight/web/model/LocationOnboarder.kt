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
import streetlight.model.data.LocationConfigContent
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

    val nameState = state.mutableTapOf({ it.name }) { copy(name = it) }
    val addressState = state.mutableTapOf({ it.address }) { copy(address = it) }
    val resultsState = state.tapOf { it.results }
    val canSearchOSMState = state.tapOf { it.name.isNotBlank() && !it.isOSM }
    val focusState = state.tapOf { it.focus }
    val selectionState = state.mutableTapOf({ it.focus as? LocationEdit }) { copy(focus = it) }
    val hostedSelectionState = state.mutableTapOf({ it.focus as? LocationConfigContent }) { copy(focus = it) }

    init {
        scope.launch("read hosted configs") {
            val hosted = api.location.readHostedConfigs().toDataOrNull(searchMessage).orEmpty()
            state.set { copy(focus = if (hosted.isEmpty()) NullLocation else HostedLocations(hosted)) }
        }

        focusState.reactIn(scope) { focus ->
            if (focus is LocationEdit) editor.editField.set(focus)
        }

        state.tapOf { it.name to it.address }.reactIn(scope) { (name, address) ->
            searchMessage.clear()
            val matches = when {
                name.isBlank() && address.isBlank() -> emptyList()
                else -> api.location.matchLocations(name, address.takeIf { it.isNotBlank() })
                    .toDataOr(searchMessage) { return@reactIn }
            }
            state.set { copy(results = matches.map { it.toEdit() }, isOSM = false) }
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

    /** Saves the design built by [designer] to the focused config, and makes the config read again the focus. */
    fun submitDesign(designer: DesignEditor, messages: MessageStore) {
        val content = stateNow.focus as? LocationConfigContent ?: return
        scope.launch(::submitDesign) {
            val design = designer.build(messages)
            messages.deliverSending()
            api.location.updateLocationConfig(content.config.copy(design = design)).toDataOr(messages) { return@launch }
            val updated = api.location.readLocationConfigContent(content.location.locationId)
                .toDataOr(messages) { return@launch }
            state.set { copy(focus = updated) }
        }
    }

    /** Searches OpenStreetMap for the name and address. */
    fun searchOSM() {
        searchMessage.deliverSending("Searching OpenStreetMap...")
        scope.launch(::searchOSM) {
            val query = listOf(stateNow.name, stateNow.address).filter { it.isNotBlank() }.joinToString(", ")
            val places = osm.readLocations(query).toDataOr(searchMessage) { return@launch }
                .mapNotNull { it.toEditOrNull() }
            if (places.isEmpty()) searchMessage.deliver("No locations found.") else searchMessage.clear()
            state.set { copy(results = places, isOSM = true) }
        }
    }
}

data class LocationOnboarderState(
    val name: String = "",
    val address: String = "",
    val results: List<LocationEdit> = emptyList(),
    val isOSM: Boolean = false,
    val focus: Any? = null,
)

/** The focus of a search for a location to onboard, before one is chosen. */
object NullLocation

/** The focus of a choice among the [locations] the star hosts. */
data class HostedLocations(val locations: List<LocationConfigContent>)