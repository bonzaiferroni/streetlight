package streetlight.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.model.GeoPoint
import pondui.ui.controls.Button
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.nav.LocalNav
import pondui.utils.current
import pondui.utils.rememberGeoLocator
import streetlight.app.AreaProfileRoute
import streetlight.app.LocationProfileRoute
import kotlin.time.Duration.Companion.seconds

@Composable
fun AreaProfileScreen(
    route: AreaProfileRoute,
    viewModel: AreaProfileModel = viewModel { AreaProfileModel(route) }
) {
    val state by viewModel.stateFlow.collectAsState()
    val nav = LocalNav.current
    val geoLocator = rememberGeoLocator()

    Scaffold {
        Row(1) {
            TextField(
                text = state.newName,
                onChange = viewModel::setNewName,
                placeholder = "New Area Name",
                modifier = Modifier.weight(1f)
            )
            Button("Create", isEnabled = state.isValidNewItem, onClick = viewModel::createNewItem)
        }
        Row(1) {
            TextField(
                state.newLongitude,
                onChange = viewModel::setNewLongitude,
                placeholder = "Longitude",
                modifier = Modifier.weight(1f)
            )
            TextField(
                state.newLatitude,
                onChange = viewModel::setNewLatitude,
                placeholder = "Latitude",
                modifier = Modifier.weight(1f)
            )
            Button("Locate", onClick = {
                geoLocator?.current(20.seconds) { location ->
                    location?.let {
                        viewModel.setGeoPoint(GeoPoint(location.longitude, location.latitude))
                    }
                }
            })
        }
        LazyColumn(1) {
            items(state.locations) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(it.name ?: "${it.geoPoint.longitude}, ${it.geoPoint.latitude}")
                    Button("➡") { nav.go(LocationProfileRoute(it.locationId.value)) }
                }
            }
        }

        Spirit("monkey-dance")
    }
}