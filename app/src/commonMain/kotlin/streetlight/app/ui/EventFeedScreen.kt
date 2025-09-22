package streetlight.app.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import pondui.ui.controls.Button
import pondui.ui.controls.Cloud
import pondui.ui.controls.Column
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.LazyScaffold
import pondui.ui.controls.Row
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.actionable
import pondui.ui.controls.rememberCloud
import streetlight.app.EventFeedRoute

@Composable
fun EventFeedScreen(
    viewModel: EventFeedModel = viewModel { EventFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    val cloudToggle = rememberCloud { toggle ->
        Column(1) {
            TextField(state.locationSearch, onValueChange = viewModel::searchLocations)
            LazyColumn(1) {
                items(state.locations) { location ->
                    Text(location.name, modifier = Modifier.actionable {
                        viewModel.create(location)
                        toggle()
                    } )
                }
            }
        }
    }

    LazyScaffold {
        item("header") {
            Row(1) {
                Button(TablerIcons.Plus, onClick = cloudToggle)
            }
        }
        items(state.events) { event ->
            Text(event.title)
        }
    }
}