package streetlight.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.Button
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.nav.LocalNav
import streetlight.app.AreaProfileRoute
import streetlight.app.LocationProfileRoute

@Composable
fun AreaProfileScreen(
    route: AreaProfileRoute,
    viewModel: AreaProfileModel = viewModel { AreaProfileModel(route) }
) {
    val state by viewModel.stateFlow.collectAsState()
    val nav = LocalNav.current

    Scaffold {
        TextField(state.newName, onChange = viewModel::setNewName, placeholder = "New Area Name")
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
            Button("Create", isEnabled = state.isValidNewItem, onClick = viewModel::createNewItem)
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