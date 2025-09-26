package streetlight.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import compose.icons.TablerIcons
import compose.icons.tablericons.Clock
import compose.icons.tablericons.ZoomCancel
import kabinet.utils.toAgoDescription
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import streetlight.model.data.*
import kotlin.time.Duration.Companion.minutes

@Composable
fun EventStatusDash(
    startsAt: Instant,
    endsAt: Instant = Clock.System.now() + 60.minutes,
    status: EventStatus,
    setStatus: (EventStatus) -> Unit,
) {
    MagicItem(status) { status ->
        when (status) {
            EventStatus.Pending -> {
                val timeUntilStart = startsAt - Clock.System.now()
                val minutesUntilStart = minOf(10, timeUntilStart.inWholeMinutes)
                val progress = (10 - minutesUntilStart) / 10f
                Row(1) {
                    Icon(TablerIcons.Clock, tint = Pond.localColors.contentDim)
                    ProgressBar(progress, padding = Pond.ruler.unitPadding) {
                        Text("Starts ${(-timeUntilStart).toAgoDescription()}")
                    }
                    Expando()
                    Button("Start") { setStatus(EventStatus.Live) }
                    MoreMenu {
                        MoreMenuItem("Cancel event", TablerIcons.ZoomCancel) { setStatus(EventStatus.Canceled) }
                    }
                }
            }
            EventStatus.Live -> {
                val timeUntilEnd = endsAt - Clock.System.now()
                val minutesUntilEnd = timeUntilEnd.inWholeMinutes
                val totalMinutes = (endsAt - startsAt).inWholeMinutes
                val progress = minutesUntilEnd / totalMinutes.toFloat()
                Row(1) {
                    Icon(TablerIcons.Clock)
                    ProgressBar(progress, padding = Pond.ruler.unitPadding) {
                        Text("Ends ${(-timeUntilEnd).toAgoDescription()}")
                    }
                    Expando()
                    Button("Take a break", color = Pond.colors.action) { setStatus(EventStatus.OnBreak) }
                    MoreMenu {
                        MoreMenuItem("Delay event") { setStatus(EventStatus.Pending) }
                        MoreMenuItem("Cancel event") { setStatus(EventStatus.Canceled) }
                    }
                }
            }
//        EventStatus.OnBreak -> TODO()
//        EventStatus.Finished -> TODO()
            else -> {
                Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxWidth()) {
                    DropMenu(status, label = "status", onChange = setStatus)
                }
            }
        }
    }
}

@Composable
@Preview
fun EventLiveAtomicPreview() {
    val now = Clock.System.now()

    MultiPreview {
        PreviewFrame("Status Dash", "Pending") {
            EventStatusDash(
                startsAt = now + 5.minutes,
                endsAt = now + 65.minutes,
                status = EventStatus.Pending,
                setStatus = {}
            )
        }
        PreviewFrame("Status Dash", "Live") {
            EventStatusDash(
                startsAt = now - 10.minutes,
                endsAt = now - 50.minutes,
                status = EventStatus.Live,
                setStatus = {}
            )
        }
//        PreviewFrame("Status Dash", "On Break") {
//            EventStatusDash(status = EventStatus.OnBreak, setStatus = {})
//        }
//        PreviewFrame("Status Dash", "Finished") {
//            EventStatusDash(status = EventStatus.Finished, setStatus = {})
//        }
    }
}