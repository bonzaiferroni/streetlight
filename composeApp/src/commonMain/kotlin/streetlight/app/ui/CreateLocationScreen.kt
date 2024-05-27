package streetlight.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import streetlight.app.chopui.Scaffold
import streetlight.model.Area
import streetlight.model.Location

class CreateLocationScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel = rememberScreenModel<CreateLocationModel>()
        val state by screenModel.state
        Scaffold("Add Location", navigator) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    TextField(
                        value = state.location.name,
                        onValueChange = screenModel::updateName,
                        label = { Text("Name") }
                    )
                    TextField(
                        value = state.location.latitude.toString(),
                        onValueChange = screenModel::updateLatitude,
                        label = { Text("Latitude") }
                    )
                    TextField(
                        value = state.location.longitude.toString(),
                        onValueChange = screenModel::updateLongitude,
                        label = { Text("Longitude") }
                    )
                    AreaChooser(state.location, state.areas, screenModel::updateArea)
                    Button(onClick = screenModel::addLocation) {
                        Text("Add Location")
                    }
                    Text(state.result)
                }
            }
        }
    }

    @Composable
    fun AreaChooser(
        location: Location,
        areas: List<Area>,
        updateArea: (Area) -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }
        val area = areas.find { it.id == location.areaId }

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                area?.let {
                    Text(it.name)
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More"
                    )
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                areas.forEach { area ->
                    DropdownMenuItem(onClick = {
                        expanded = false
                        updateArea(area)
                    }) {
                        Text(area.name)
                    }
                }
            }
        }
    }
}