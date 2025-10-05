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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Column
import pondui.ui.controls.FlowRow
import pondui.ui.controls.H1
import pondui.ui.controls.InProgressIndicator
import pondui.ui.controls.Row
import pondui.ui.controls.Tab
import pondui.ui.controls.TabScaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.TimeWheel
import pondui.ui.controls.UpdateStatus
import pondui.utils.SinglePreview
import streetlight.app.EventProfileRoute
import streetlight.model.data.Event
import streetlight.model.data.toProjectId
import streetlight.model.mockDb

@Composable
fun EventProfileScreen(
    route: EventProfileRoute,
    viewModel: EventProfileModel = viewModel (key = route.id) { EventProfileModel(route.id.toProjectId()) }
) {
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
                UpdateIndicator(
                    updatedAt = event.updatedAt,
                    updateStatus = state.updateStatus,
                )
                EventProfileForm(
                    event = event,
                    setTitle = viewModel::setTitle,
                    setDescription = viewModel::setDescription,
                    setStartsAt = viewModel::setStartsAt,
                    setEndsAt = viewModel::setEndsAt,
                )
            }
        }
        Tab("Live") {
            LiveEventView(
                eventId = event.eventId,
                startsAt = event.startsAt,
                endsAt = event.endsAt,
                status = event.status,
                setStatus = viewModel::setStatus,
            )
        }
    }
}

@Composable
fun EventProfileForm(
    event: Event,
    setTitle: (String) -> Unit,
    setDescription: (String) -> Unit,
    setStartsAt: (Instant) -> Unit,
    setEndsAt: (Instant) -> Unit,
) {
    FlowRow(1, maxItemsInEachRow = 2) {
        TextField(
            event.title,
            label = "title",
            placeholder = "title",
            onChange = setTitle,
            modifier = Modifier.weight(1f)
        )
    }
    TextField(
        event.description ?: "",
        label = "description",
        placeholder = "description",
        onChange = setDescription,
        modifier = Modifier.fillMaxWidth()
    )
    TimeWheel(event.startsAt, onChangeInstant = setStartsAt)
    TimeWheel(event.endsAt, onChangeInstant = setEndsAt)

    // TextField("$APP_API_URL/eventportal/${event.eventId.value}") { }
}

@Composable
fun UpdateIndicator(
    updatedAt: Instant,
    updateStatus: UpdateStatus,
) {
    Row(1) {
        Text("Updated at ${updatedAt.toTimeDescription()}")
        InProgressIndicator(updateStatus)
    }
}

@Composable
@Preview()
fun EventProfilePreview() {
    SinglePreview {
        UpdateIndicator(
            updatedAt = Clock.System.now(),
            updateStatus = UpdateStatus.Done,
        )
        EventProfileForm(
            event = mockDb.events.first(),
            setTitle = {},
            setDescription = {},
            setStartsAt = {},
            setEndsAt = {},
        )
    }
}