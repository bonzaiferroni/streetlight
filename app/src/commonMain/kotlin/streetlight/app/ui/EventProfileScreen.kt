package streetlight.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.toHourAndMinutesFormat
import kotlin.time.Clock
import kotlin.time.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.Column
import pondui.ui.controls.FlowRow
import pondui.ui.controls.H1
import pondui.ui.controls.InProgressIndicator
import pondui.ui.controls.Row
import pondui.ui.controls.ScaffoldTab
import pondui.ui.controls.TabScaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.TimeWheel
import pondui.ui.controls.UpdateStatus
import pondui.utils.SinglePreview
import streetlight.app.EventProfileRoute
import streetlight.model.data.Event
import streetlight.model.data.toRecordId
import streetlight.model.mockDb

@Composable
fun EventProfileScreen(
    route: EventProfileRoute,
    viewModel: EventProfileModel = viewModel(key = route.id + "profile") { EventProfileModel(route.id.toRecordId()) }
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
        ScaffoldTab("Live") {
            LiveEventView(
                event = event,
                modifyEvent = viewModel::updateEvent
            )
        }
        ScaffoldTab("Edit") {
            Column(2) {
                UpdateIndicator(
                    updatedAt = event.updatedAt,
                    updateStatus = state.updateStatus,
                )
                EventProfileForm(
                    event = event,
                    modifyEvent = viewModel::updateEvent,
                )
            }
        }
    }
}

@Composable
fun EventProfileForm(
    event: Event,
    modifyEvent: (Event) -> Unit,
) {
    FlowRow(1, maxItemsInEachRow = 2) {
        TextField(
            event.title,
            label = "title",
            placeholder = "title",
            modifier = Modifier.weight(1f)
        ) { modifyEvent(event.copy(title = it)) }
    }
    TextField(
        event.description ?: "",
        label = "description",
        placeholder = "description",
        modifier = Modifier.fillMaxWidth()
    ) { modifyEvent(event.copy(description = it)) }
    TimeWheel(event.startsAt) { modifyEvent(event.copy(startsAt = it)) }
    TimeWheel(event.endsAt) { modifyEvent(event.copy(endsAt = it)) }
    TextField(event.cashTips) { modifyEvent(event.copy(cashTips = it)) }
    TextField(event.cardTips) { modifyEvent(event.copy(cardTips = it)) }
}

@Composable
fun UpdateIndicator(
    updatedAt: Instant,
    updateStatus: UpdateStatus,
) {
    Row(1) {
        Text("Updated at ${updatedAt.toHourAndMinutesFormat()}")
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
            modifyEvent = {}
        )
    }
}