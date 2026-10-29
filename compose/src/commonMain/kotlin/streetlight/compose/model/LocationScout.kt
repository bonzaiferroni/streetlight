package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.model.MessageStore
import koala.model.Toaster
import kotlinx.coroutines.launch
import streetlight.compose.io.OsmClient
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationEntity
import streetlight.model.data.PostEdit
import streetlight.model.data.PostType
import streetlight.model.io.ApiClient

/**
 * Finds or starts a location with its [finder], to post it to [galaxy], when given. A new location is written in its
 * [editor]. The focus is what the star is working on, and its stage follows from it. The cousin of the web's
 * `LocationScout`.
 */
class LocationScout(
    val galaxy: Galaxy?,
    private val api: ApiClient,
    osm: OsmClient,
    geoMap: GeoMap,
    settings: AppSettings,
    private val toaster: Toaster,
) : ViewModel() {
    private val store = storeOf(LocationScoutState())

    val finder = LocationFinder(viewModelScope, galaxy, api, osm, geoMap, toaster, ::focus)
    val editor = LocationEditor(api)
    val focusState = store.tapOf { it.focus }
    val stageState = store.tapOf { it.focus.toStage() }
    val postMessage = MessageStore()
    val isPostedState = store.tapOf { it.isPosted }
    val postModeState = settings.locationPostModeState
    val feedModeState = settings.feedModeState

    /** Works on [location]: reviews one of Streetlight's, or starts editing one that is new. */
    fun focus(location: LocationEntity) {
        val focus = when (location) {
            is Location -> LocationScoutFocus.Review(location)
            is LocationEdit -> LocationScoutFocus.Edit.also { editor.setEdit(location) }
        }
        store.set { copy(focus = focus) }
    }

    /** Reviews the new location, once its edit is valid. */
    fun review() {
        if (!editor.isEditValid()) return
        store.set { copy(focus = LocationScoutFocus.Review(editor.editNow)) }
    }

    fun resumeEdit() = store.set { copy(focus = LocationScoutFocus.Edit) }

    /** Returns to the search, keeping what was found. */
    fun search() = store.set { copy(focus = null) }

    /** Saves the location when it is new, posts it to the galaxy when there is one, and goes on by the post mode. */
    fun post() {
        val focus = store.now.focus as? LocationScoutFocus.Review ?: return
        postMessage.clear()
        viewModelScope.launch {
            val location = when (val location = focus.location) {
                is Location -> location
                is LocationEdit -> editor.saveLocation()?.also {
                    store.set { copy(focus = LocationScoutFocus.Review(it)) }
                }
            } ?: return@launch
            galaxy?.let { galaxy ->
                val edit = PostEdit(null, galaxy.galaxyId, PostType.Location, location.locationId.value, null)
                api.post.createPost(edit).toDataOr(postMessage) { return@launch }
            }
            when (postModeState.now) {
                LocationPostMode.ResetLocation -> {
                    toaster.deliverSuccess("Posted! Ready for the next one.")
                    reset()
                }
                LocationPostMode.Return -> {
                    val posted = galaxy?.let { "Posted to ${it.name}." } ?: "Posted ${location.name ?: "Location"}."
                    toaster.deliverSuccess(posted)
                    store.set { copy(isPosted = true) }
                }
            }
        }
    }

    /** Returns to the search with nothing chosen or found. */
    fun reset() {
        store.set { LocationScoutState() }
        finder.reset()
        editor.setEdit(LocationEdit())
        postMessage.clear()
    }
}

data class LocationScoutState(
    val focus: LocationScoutFocus? = null,
    val isPosted: Boolean = false,
)

/** The location a [LocationScout] works on, past the search. */
sealed interface LocationScoutFocus {
    /** A new location, its details being written in the scout's editor. */
    data object Edit : LocationScoutFocus

    /** A [location] ready to post: one of Streetlight's, or a new one as written. */
    data class Review(val location: LocationEntity) : LocationScoutFocus
}

/** The stages of a [LocationScout], in order, each with its [label]. */
enum class LocationScoutStage(val label: String) {
    Search("Search"),
    Edit("Edit"),
    Review("Review"),
}

/** The stage of this focus: the search without one. */
fun LocationScoutFocus?.toStage() = when (this) {
    null -> LocationScoutStage.Search
    is LocationScoutFocus.Edit -> LocationScoutStage.Edit
    is LocationScoutFocus.Review -> LocationScoutStage.Review
}
