package streetlight.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.Text
import pondui.ui.controls.TextButton
import pondui.ui.controls.TextField
import pondui.ui.nav.LocalNav
import pondui.ui.nav.Scaffold
import pondui.ui.theme.Pond
import streetlight.app.AreaProfileRoute
import streetlight.app.LocationProfileRoute

@Composable
fun AreaProfileScreen(
    route: AreaProfileRoute,
    viewModel: AreaProfileModel = viewModel { AreaProfileModel(route) }
) {
    val state by viewModel.state.collectAsState()
    val nav = LocalNav.current

    Scaffold {
        TextField(state.newName, viewModel::setNewName, "New Area Name")
        Row(
            horizontalArrangement = Pond.ruler.rowTight
        ) {
            TextField(state.newLongitude, viewModel::setNewLongitude, "Longitude", modifier = Modifier.weight(1f))
            TextField(state.newLatitude, viewModel::setNewLatitude, "Latitude", modifier = Modifier.weight(1f))
            TextButton("Create", state.isValidNewItem, onClick = viewModel::createNewItem)
        }
        LazyColumn(
            verticalArrangement = Pond.ruler.columnTight
        ) {
            items(state.locations) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(it.name ?: "${it.geoPoint.longitude}, ${it.geoPoint.latitude}")
                    TextButton("➡") { nav.go(LocationProfileRoute(it.id)) }
                }
            }
        }
    }
}