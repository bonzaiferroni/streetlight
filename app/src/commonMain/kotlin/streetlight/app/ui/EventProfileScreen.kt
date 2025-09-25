package streetlight.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.toTimeDescription
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.FlowRow
import pondui.ui.controls.H1
import pondui.ui.controls.InProgressIndicator
import pondui.ui.controls.Label
import pondui.ui.controls.Row
import pondui.ui.controls.Tab
import pondui.ui.controls.TabScaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.TimeWheel
import streetlight.app.EventProfileRoute
import streetlight.model.data.toProjectId

@Composable
fun EventProfileScreen(
    route: EventProfileRoute,
    viewModel: EventProfileModel = viewModel { EventProfileModel() }
) {
    viewModel.init(route.id.toProjectId())
    val state by viewModel.stateFlow.collectAsState()

    val event = state.event ?: return

    TabScaffold(
        drawerContent = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                H1(event.title)
            }
        }
    ) {
        Tab("Edit") {
            Column(2) {
                Row(1) {
                    Label("Updated at ${event.updatedAt.toTimeDescription()}")
                    InProgressIndicator(state.updateStatus)
                }
                FlowRow(1, maxItemsInEachRow = 2) {
                    TextField(
                        event.title,
                        label = "title",
                        placeholder = "title",
                        onChange = viewModel::setTitle,
                        modifier = Modifier.weight(1f)
                    )
                }
                TextField(
                    event.description ?: "",
                    label = "description",
                    placeholder = "description",
                    onChange = viewModel::setDescription,
                    modifier = Modifier.fillMaxWidth()
                )
                TimeWheel(event.startsAt, onChangeInstant = viewModel::setStartsAt)
                TimeWheel(event.endsAt, onChangeInstant = viewModel::setEndsAt)
            }
        }
        Tab("Live") {
            EventLiveView(viewModel)
        }
    }
}
