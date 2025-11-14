package streetlight.app.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.LazyScaffold
import pondui.ui.controls.MoreMenu
import pondui.ui.controls.MoreMenuItem
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.actionable
import pondui.ui.controls.rememberCloud
import pondui.ui.modifiers.pad
import pondui.ui.modifiers.padVertical
import streetlight.app.utils.toRoute

@Composable
fun EventFeedScreen(
    viewModel: EventFeedModel = viewModel { EventFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    val cloudToggle = rememberCloud(title = "Where?") { toggle ->
        Column(1) {
            TextField(
                text = state.locationSearch,
                onChange = viewModel::searchLocations,
            )
            DropMenu(state.eventType, {it.label}, onChange = viewModel::setEventType)
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
            Row(1) {
                Text(
                    text = event.title,
                    modifier = Modifier.weight(1f)
                        .actionable(event.eventId.toRoute())
                        .padVertical(1)
                )
                MoreMenu {
                    MoreMenuItem("Remove", TablerIcons.Trash) { viewModel.removeEvent(event) }
                }
            }
        }
    }
}