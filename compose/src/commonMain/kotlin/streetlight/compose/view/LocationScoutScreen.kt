package streetlight.compose.view

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import koala.SvgFile
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.LocationScout
import streetlight.compose.model.LocationScoutFocus
import streetlight.compose.model.LocationScoutRoute
import streetlight.compose.model.LocationScoutStage
import streetlight.compose.model.PostGalaxy
import streetlight.compose.model.currentPortal
import streetlight.compose.model.toRoute
import streetlight.compose.model.toStage
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState
import streetlight.model.data.EntityType
import streetlight.model.data.FeedContext
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationEntity
import streetlight.model.data.Star

/**
 * The screen of [route]: its name as its top bar, and, while a star is signed in, the galaxy the location goes to once
 * read, if any, scouted in the [LocationScoutView].
 */
@Composable
fun LocationScoutScreen(route: LocationScoutRoute) {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.TwoTone("Location", "Scout"), scrollState)
    StarGate {
        Inflated<PostGalaxy>(route) { target ->
            context(koinViewModel<LocationScout> { parametersOf(target) }) { LocationScoutView(scrollState) }
        }
    }
}

/**
 * The view of [LocationScoutRoute], in a column scrolled by [scrollState] under the top bar: the name of the galaxy the
 * location is posted to, if any, the stages of the scout with the one at hand lit, and that stage's form. The cousin
 * of the web's `viewLocationScout`.
 */
@Composable
context(model: LocationScout, star: Star)
fun LocationScoutView(scrollState: ScrollState) {
    val stage by model.stageState.collectAsState()
    val isPosted by model.isPostedState.collectAsState()
    val portal = currentPortal
    // the scout gives its place to where it was headed, as the web's goes on to it
    LaunchedEffect(isPosted) {
        if (isPosted) portal.swap(model.galaxy?.toRoute() ?: HomeRoute)
    }

    ConfigBody(model.galaxy?.name, scrollState) {
        StageHeader(LocationScoutStage.entries.map { it.label }, stage.ordinal)
        when (stage) {
            LocationScoutStage.Search -> LocationFinder()
            LocationScoutStage.Edit -> LocationEditStage()
            LocationScoutStage.Review -> LocationReviewStage()
        }
    }
}

/**
 * The search for a location, by its name and city in Streetlight, or in OpenStreetMap on request, and the map to find
 * it where it lies or put a new one there, in the location's scheme color. The cousin of the web's `locationFinder`.
 */
@Composable
context(model: LocationScout)
private fun LocationFinder() {
    val theme = currentTheme
    CompositionLocalProvider(LocalSchemeColor provides theme.colors.colorOf(EntityType.Location)) {
        FormCard("Location Finder") {
            ColumnRow {
                FormSection("search") { LocationSearch() }
                FormSection("or just put it on the map") { LocationMap() }
            }
        }
    }
}

/** The fields of the query and the city, and the locations they find, or a way to search further. */
@Composable
context(model: LocationScout)
private fun LocationSearch() {
    val theme = currentTheme
    val query by model.queryState.collectAsState()
    val locations by model.locationsState.collectAsState()

    Row(1, Modifier.fillMaxWidth()) {
        TextField(model.queryState, "search", Modifier.weight(1f), icon = SvgFile.Search)
        TextField(model.cityState, "city", Modifier.width(theme.unit * CityFieldUnits), icon = SvgFile.City)
    }
    SelectorContainer {
        locations.forEach { location ->
            SearchItem(location, Modifier.pointerClick { model.focus(location) })
        }
        when (query.isEmpty()) {
            true -> CenteredNote("Start typing in the search box to see locations")
            false -> {
                CenteredNote("Don't see the location in the list? Try a power search with OpenStreetMap.")
                Column(1, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button("Search OSM", onClick = model::queryOsm)
                    MessageBox(model.queryMessage)
                }
            }
        }
    }
}

/**
 * A [location] found in a search: its pin, its name over its address, and its city when known, in the scheme color.
 * The cousin of the web's `searchItem`.
 */
@Composable
private fun SearchItem(location: LocationEntity, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val scheme = LocalSchemeColor.current
    Row(1, modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(SvgFile.MapPin.large, color = { scheme }, size = theme.unit * 5)
        Column(0, Modifier.weight(1f)) {
            BasicText(
                location.name ?: "a location",
                style = theme.text.regular.copy(fontWeight = FontWeight.SemiBold),
                color = { theme.colors.ink },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            location.address?.let {
                BasicText(
                    it,
                    style = theme.text.small,
                    color = { theme.colors.ink },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        location.city?.let { city ->
            Icon(SvgFile.City.small, color = { scheme })
            BasicText(
                city,
                Modifier.width(theme.unit * CityLabelUnits).opacityHigh(),
                style = theme.text.small,
                color = { theme.colors.ink },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The map, looking over the galaxy when there is one, with the markers of what was found, then the ways to ask what is
 * at its center and to start a new location there, named with the query.
 */
@Composable
context(model: LocationScout)
private fun LocationMap() {
    val theme = currentTheme

    MapMount(
        Modifier.fillMaxWidth().height(theme.unit * MapHeightUnits),
        center = model.galaxy?.geoPoint,
        markerMap = model.markerMap,
    )
    Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Button("What is Here?", onClick = model::whatIsHere)
        MessageBox(model.mapMessage, Modifier.weight(1f, fill = false))
    }
    Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextField(model.queryState, "name", Modifier.weight(1f), icon = SvgFile.MapPin)
        Button("Create", onClick = model::createLocation, style = ButtonStyle.Accent)
    }
}

/**
 * The edit of the new location in the scout's editor, then the way back to the search, the editor's message, and the
 * way on to the review, once the edit is valid.
 */
@Composable
context(model: LocationScout)
private fun LocationEditStage() {
    Column(2) {
        context(model.editor) { LocationEditForm() }
        Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button("Back", onClick = model::search, style = ButtonStyle.Zen)
            Box(Modifier.weight(1f)) { MessageBox(model.editor.messages) }
            Button("Next", onClick = model::review)
        }
    }
}

/**
 * The location as it will be posted, then the post mode, the way back to the edit or the search, the last message, and
 * the way to post. The cousin of the web's review stage.
 */
@Composable
context(model: LocationScout, star: Star)
private fun LocationReviewStage() {
    val focus by model.focusState.collectAsState()
    val mode by model.feedModeState.collectAsState()
    val isSaving by model.editor.isSavingState.collectAsState()
    val location = (focus as? LocationScoutFocus.Review)?.location ?: return

    Column(2) {
        when (location) {
            is Location -> context(null as FeedContext?) { FeedRow(location, mode) }
            is LocationEdit -> PostRow(location, star.username, mode)
        }
        Column(1, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            DropMenu(model.postModeState)
            Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                when (location) {
                    is LocationEdit -> Button("Edit", onClick = model::edit, style = ButtonStyle.Zen)
                    is Location -> Button("Back to search", onClick = model::search, style = ButtonStyle.Zen)
                }
                Column(1, Modifier.weight(1f)) {
                    MessageBox(model.editor.messages)
                    MessageBox(model.postMessage)
                }
                Button("Post", onClick = model::post, isEnabled = !isSaving)
            }
        }
    }
}

/** [text] centered across its column. */
@Composable
private fun CenteredNote(text: String) {
    val theme = currentTheme
    BasicText(
        text,
        Modifier.fillMaxWidth(),
        style = theme.text.regular.copy(textAlign = TextAlign.Center),
        color = { theme.colors.ink },
    )
}

/** The width of the city's field in units, the web's `Width(24)`. */
private const val CityFieldUnits = 24

/** The width of a found location's city in units, the web's `Width(16)`. */
private const val CityLabelUnits = 16

/** The height of the map in units. */
private const val MapHeightUnits = 40
