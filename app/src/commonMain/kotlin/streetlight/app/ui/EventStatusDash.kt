package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CalendarTime
import compose.icons.tablericons.Check
import compose.icons.tablericons.CircleX
import compose.icons.tablericons.Clock
import compose.icons.tablericons.Flame
import compose.icons.tablericons.PlayerPause
import kabinet.utils.toAgoFormat
import kabinet.utils.toHourAndMinutesFormat
import kotlin.time.Clock
import kotlin.time.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.controls.*
import pondui.ui.modifiers.MagicItem
import pondui.ui.services.rememberTextSpeaker
import pondui.ui.theme.Pond
import pondui.utils.MultiPreview
import pondui.utils.PreviewFrame
import pondui.utils.electrify
import streetlight.model.data.*
import streetlight.model.mockDb
import kotlin.time.Duration.Companion.minutes

@Composable
fun EventStatusDash(
    event: Event,
    breakStartedAt: Instant?,
    setStatus: (EventStatus) -> Unit,
    toggleBreak: () -> Unit,
) {
    val status = event.status; val startsAt = event.startsAt; val endsAt = event.endsAt
    val now = Clock.System.now()
    val speak = rememberTextSpeaker()
    val setStatus: (EventStatus) -> Unit = {
        setStatus(it)
        speak(it.label)
    }
    Section {
        Column(1) {
            Row(1) {
                MagicItem(status, offsetY = (-5).dp) {
                    val (icon, color) = status.toIconConfig()
                    Icon(icon, color = color)
                }
                H3("Status:", color = Pond.localColors.contentDim)
                MagicItem(status, offsetY = 5.dp) { status ->
                    H3(status.label)
                }
                Expando()
                Column(0, horizontalAlignment = Alignment.End) {
                    Row(1) {
                        Label("start")
                        Text(startsAt.toHourAndMinutesFormat(), style = Pond.typo.small)
                    }
                    Row(1) {
                        Label("end")
                        Text(endsAt.toHourAndMinutesFormat(), style = Pond.typo.small)
                    }
                }
            }
            MagicItem(status, offsetX = 20.dp, scale = .8f) { status ->
                when (status) {
                    EventStatus.Pending -> {
                        val timeUntilStart = startsAt - now
                        val minutesUntilStart = minOf(10, timeUntilStart.inWholeMinutes)
                        val progress = (10 - minutesUntilStart) / 10f
                        Row(1) {
                            Icon(TablerIcons.Clock, color = Pond.localColors.contentDim)
                            ProgressBar(progress, padding = Pond.ruler.unitPadding) {
                                Text("Starts ${(-timeUntilStart).toAgoFormat()}", style = Pond.typo.small)
                            }
                            Expando()
                            MoreMenu {
                                MoreMenuItem("Cancel event", TablerIcons.CircleX, Pond.colors.negation.electrify()) {
                                    setStatus(
                                        EventStatus.Canceled
                                    )
                                }
                            }
                            Button("Start") { setStatus(EventStatus.Live) }
                        }
                    }

                    EventStatus.Canceled -> {
                        Row(1) {
                            Label("Event has been canceled")
                            Expando()
                            Button("Uncancel") { setStatus(EventStatus.Pending) }
                        }
                    }

                    EventStatus.Live -> {
                        val timeUntilEnd = endsAt - now
                        val minutesUntilEnd = maxOf(0, timeUntilEnd.inWholeMinutes)
                        val totalMinutes = (endsAt - startsAt).inWholeMinutes
                        val progress = 1 - minutesUntilEnd / totalMinutes.toFloat()
                        Row(1) {
                            Icon(TablerIcons.Clock, color = Pond.localColors.contentDim)
                            ProgressBar(progress, padding = Pond.ruler.unitPadding) {
                                Text("Ends ${(-timeUntilEnd).toAgoFormat()}", style = Pond.typo.small)
                            }
                            Expando()
                            MoreMenu {
                                MoreMenuItem(
                                    label = "Delay event",
                                    icon = TablerIcons.CalendarTime,
                                    color = Pond.colors.primary.electrify()
                                ) { setStatus(EventStatus.Pending) }
                                if (minutesUntilEnd > 0) {
                                    MoreMenuItem(
                                        label = "Finish event",
                                        icon = TablerIcons.Check,
                                        color = Pond.colors.primary.electrify()
                                    ) { setStatus(EventStatus.Finished) }
                                } else {
                                    MoreMenuItem(
                                        "Take a break",
                                        TablerIcons.PlayerPause
                                    ) { setStatus(EventStatus.OnBreak) }
                                }
                            }
                            if (minutesUntilEnd > 0) {
                                Button("Take a break", color = Pond.colors.primary) {
                                    setStatus(EventStatus.OnBreak)
                                    toggleBreak()
                                }
                            } else {
                                Button("Finish") {
                                    setStatus(EventStatus.Finished)
                                }
                            }
                        }
                    }

                    EventStatus.OnBreak -> {
                        val breakStartedAt = breakStartedAt ?: now
                        val breakEndsAt = breakStartedAt + 5.minutes
                        val timeUntilEnd = breakEndsAt - now
                        val minutesUntilEnd = timeUntilEnd.inWholeMinutes
                        val progress = minutesUntilEnd / 5f
                        Row(1) {
                            Icon(TablerIcons.Clock, color = Pond.localColors.contentDim)
                            ProgressBar(progress, padding = Pond.ruler.unitPadding) {
                                Text("Ends ${(-timeUntilEnd).toAgoFormat()}", style = Pond.typo.small)
                            }
                            Expando()
                            MoreMenu {
                                MoreMenuItem("Delay event") { setStatus(EventStatus.Pending) }
                                MoreMenuItem("Finish event") { setStatus(EventStatus.Finished) }
                            }
                            Button("Resume event", color = Pond.colors.primary) {
                                setStatus(EventStatus.Live)
                                toggleBreak()
                            }
                        }
                    }

                    EventStatus.Finished -> {
                        val timeSinceEnd = now - endsAt
                        Row(1) {
                            Label("Finished ${timeSinceEnd.toAgoFormat()}")
                            Expando()
                            MoreMenu {
                                MoreMenuItem("Encore", TablerIcons.Flame, Pond.colors.accent) { setStatus(EventStatus.Live) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventStatus.toIconConfig() = when (this) {
    EventStatus.Pending -> TablerIcons.CalendarTime to Pond.colors.primary.electrify()
    EventStatus.Canceled -> TablerIcons.CircleX to Pond.colors.negation.electrify()
    EventStatus.Live -> TablerIcons.Flame to Pond.colors.accent.electrify()
    EventStatus.OnBreak -> TablerIcons.PlayerPause to Pond.colors.primary.electrify()
    EventStatus.Finished -> TablerIcons.Check to Pond.colors.primary.electrify()
}

@Composable
@Preview
fun EventLiveAtomicPreview() {
    val now = Clock.System.now()
    val event = mockDb.events.first()
    MultiPreview {
        PreviewFrame("Status Dash", "Pending") {
            EventStatusDash(
                event = event.copy(
                    startsAt = now + 5.minutes,
                    endsAt = now + 65.minutes,
                    status = EventStatus.Pending,
                ),
                breakStartedAt = null,
                setStatus = {},
                toggleBreak = {},
            )
        }
        PreviewFrame("Status Dash", "Pending") {
            EventStatusDash(
                event = event.copy(
                    startsAt = now + 5.minutes,
                    endsAt = now + 65.minutes,
                    status = EventStatus.Canceled,
                ),
                breakStartedAt = null,
                setStatus = {},
                toggleBreak = {},
            )
        }
        PreviewFrame("Status Dash", "Live") {
            EventStatusDash(
                event.copy(
                    startsAt = now - 10.minutes,
                    endsAt = now + 50.minutes,
                    status = EventStatus.Live,
                ),
                breakStartedAt = null,
                setStatus = {},
                toggleBreak = {},
            )
        }
        PreviewFrame("Status Dash", "On Break") {
            EventStatusDash(
                event = event.copy(
                    startsAt = now - 10.minutes,
                    endsAt = now + 50.minutes,
                    status = EventStatus.OnBreak,
                ),
                breakStartedAt = now - 2.minutes,
                setStatus = {},
                toggleBreak = {},
            )
        }
        PreviewFrame("Status Dash", "Finished") {
            EventStatusDash(
                event = event.copy(
                    startsAt = now - 70.minutes,
                    endsAt = now - 10.minutes,
                    status = EventStatus.Finished,
                ),
                breakStartedAt = null,
                setStatus = {},
                toggleBreak = {},
            )
        }
    }
}