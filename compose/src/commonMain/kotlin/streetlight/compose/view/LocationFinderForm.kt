package streetlight.compose.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import koala.SvgFile
import streetlight.compose.model.LocationFinder
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState
import streetlight.model.data.EntityType
import streetlight.model.data.Location
import streetlight.model.data.LocationEntity

/**
 * The search for a location, by its name and city in Streetlight, or in OpenStreetMap on request, and the map to find
 * it where it lies or put a new one there, in the location's scheme color. The cousin of the web's `locationFinder`.
 */
@Composable
context(finder: LocationFinder)
fun LocationFinderForm() {
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
context(finder: LocationFinder)
private fun LocationSearch() {
    val theme = currentTheme
    val query by finder.queryState.collectAsState()
    val locations by finder.locationsState.collectAsState()

    Row(1, Modifier.fillMaxWidth()) {
        TextField(finder.queryState, "search", Modifier.weight(1f), icon = SvgFile.Search)
        TextField(finder.cityState, "city", Modifier.width(theme.unit * CityFieldUnits), icon = SvgFile.City)
    }
    SelectorContainer {
        locations.forEach { location ->
            SearchItem(location, Modifier.pointerClick { finder.pick(location) })
        }
        when (query.isEmpty()) {
            true -> CenteredText("Start typing in the search box to see locations")
            false -> {
                CenteredText("Don't see the location in the list? Try a power search with OpenStreetMap.")
                Column(1, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button("Search OSM", onClick = finder::queryOsm)
                    MessageBox(finder.queryMessage)
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
context(finder: LocationFinder)
private fun LocationMap() {
    val theme = currentTheme

    MapMount(
        Modifier.fillMaxWidth().height(theme.unit * MapHeightUnits),
        center = finder.galaxy?.geoPoint,
        markerMap = finder.markerMap,
    )
    Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Button("What is Here?", onClick = finder::whatIsHere)
        MessageBox(finder.mapMessage, Modifier.weight(1f, fill = false))
    }
    Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextField(finder.queryState, "name", Modifier.weight(1f), icon = SvgFile.MapPin)
        Button("Create", onClick = finder::createLocation, style = ButtonStyle.Accent)
    }
}

/** The width of the city's field in units, the web's `Width(24)`. */
private const val CityFieldUnits = 24

/** The width of a found location's city in units, the web's `Width(16)`. */
private const val CityLabelUnits = 16

/** The height of the map in units. */
private const val MapHeightUnits = 40
