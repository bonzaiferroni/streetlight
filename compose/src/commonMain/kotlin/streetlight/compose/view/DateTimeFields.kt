package streetlight.compose.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import kabinet.utils.toFutureFormat
import kabinet.utils.toTimeFormat
import kampfire.model.MutableTap
import koala.SvgFile
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState
import kotlin.time.Clock

/** A field of the date of [state], picked in the platform's way. The cousin of the web's `dateInput`. */
@Composable
expect fun DateField(state: MutableTap<LocalDate?>, modifier: Modifier = Modifier)

/**
 * A field of the time of [state], picked in the platform's way, cleared when [isOptional]. The cousin of the web's
 * `timeInput`.
 */
@Composable
expect fun TimeField(state: MutableTap<LocalTime?>, modifier: Modifier = Modifier, isOptional: Boolean = false)

/** A [DateField] that opens a month to pick its day from, for a platform without a picker of its own. */
@Composable
fun PopoverDateField(state: MutableTap<LocalDate?>, modifier: Modifier = Modifier) {
    val date by state.collectAsState()
    var isOpen by remember { mutableStateOf(false) }

    Box(modifier) {
        Button(
            date?.let { "${it.month.ordinal + 1}/${it.day}/${it.year}" } ?: "date",
            onClick = { isOpen = true },
            style = ButtonStyle.Zen,
            flair = SvgFile.Calendar,
        )
        Popover(isOpen, onDismiss = { isOpen = false }) {
            MonthPicker(date) {
                state.set(it)
                isOpen = false
            }
        }
    }
}

/**
 * A [TimeField] that opens columns of hours and minutes to pick from, for a platform without a picker of its own, with
 * a way to clear it when [isOptional].
 */
@Composable
fun PopoverTimeField(state: MutableTap<LocalTime?>, modifier: Modifier = Modifier, isOptional: Boolean = false) {
    val time by state.collectAsState()
    var isOpen by remember { mutableStateOf(false) }

    Box(modifier) {
        Button(time?.toTimeFormat() ?: "time", onClick = { isOpen = true }, style = ButtonStyle.Zen, flair = SvgFile.Clock)
        Popover(isOpen, onDismiss = { isOpen = false }) {
            Column(1, Modifier.padding(1), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(1) {
                    PickerColumn((0 until 24).toList(), time?.hour, ::hourLabelOf) {
                        state.set(LocalTime(it, time?.minute ?: 0))
                    }
                    PickerColumn((0 until 60 step MinuteStep).toList(), time?.minute, { ":${it.twoDigits()}" }) {
                        state.set(LocalTime(time?.hour ?: NoonHour, it))
                        isOpen = false
                    }
                }
                if (isOptional) ActionButton("clear", onClick = {
                    state.set(null)
                    isOpen = false
                })
            }
        }
    }
}

/** The date of [state] with buttons that step it by a day, from today when unset. The web's `dayIndicator`. */
@Composable
fun DayStepper(state: MutableTap<LocalDate?>) {
    val date by state.collectAsState()
    Stepper(date?.toFutureFormat() ?: "someday", isSet = date != null) { delta ->
        state.set((date ?: today()).plus(delta, DateTimeUnit.DAY))
    }
}

/**
 * The time of [state] with buttons that step it by half an hour, from noon when unset, reading [unsetLabel] until it
 * is set. The web's `timeIndicator`.
 */
@Composable
fun TimeStepper(state: MutableTap<LocalTime?>, unsetLabel: String) {
    val time by state.collectAsState()
    Stepper(time?.toTimeFormat() ?: unsetLabel, isSet = time != null) { delta ->
        val seconds = (time ?: LocalTime(NoonHour, 0)).toSecondOfDay() + delta * StepMinutes * 60
        state.set(LocalTime.fromSecondOfDay(seconds.mod(SecondsPerDay)))
    }
}

/** [label] between arrows that call [onStep] with -1 or 1, faint while not [isSet]. */
@Composable
private fun Stepper(label: String, isSet: Boolean, onStep: (Int) -> Unit) {
    val theme = currentTheme
    val ink = { theme.colors.ink }
    Row(1, verticalAlignment = Alignment.CenterVertically) {
        Icon(SvgFile.ArrowLeft.large, ink, Modifier.pointerClick { onStep(-1) }.opacityHigh(), theme.unit * 4)
        AnimatedContent(
            label,
            Modifier.width(theme.unit * StepperLabelUnits),
            transitionSpec = { fadeIn(theme.magic.eased()) togetherWith fadeOut(theme.magic.eased()) },
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                it,
                style = theme.text.heading5.copy(textAlign = TextAlign.Center),
                color = { if (isSet) theme.colors.ink else theme.colors.ink.copy(alpha = theme.opacity.half) },
            )
        }
        Icon(SvgFile.ArrowRight.large, ink, Modifier.pointerClick { onStep(1) }.opacityHigh(), theme.unit * 4)
    }
}

/** A month of days, starting at [date]'s or today's, whose arrows step the month and whose days call [onPick]. */
@Composable
private fun MonthPicker(date: LocalDate?, onPick: (LocalDate) -> Unit) {
    val theme = currentTheme
    val ink = { theme.colors.ink }
    val today = today()
    var month by remember { mutableStateOf((date ?: today).let { LocalDate(it.year, it.month, 1) }) }
    val days = month.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).day
    // the weeks start on Sunday, as a calendar on the wall
    val offset = month.dayOfWeek.isoDayNumber % 7
    val cell = Modifier.size(theme.unit * DayCellUnits)

    Column(1, Modifier.padding(1), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(1, verticalAlignment = Alignment.CenterVertically) {
            Icon(SvgFile.ChevronLeft.large, ink, Modifier.pointerClick { month = month.minus(1, DateTimeUnit.MONTH) })
            BasicText(
                "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                Modifier.width(theme.unit * MonthLabelUnits),
                style = theme.text.heading5.copy(textAlign = TextAlign.Center),
                color = ink,
            )
            Icon(SvgFile.ChevronRight.large, ink, Modifier.pointerClick { month = month.plus(1, DateTimeUnit.MONTH) })
        }
        Row(0) {
            WeekdayLetters.forEach { letter ->
                Box(cell, contentAlignment = Alignment.Center) {
                    BasicText(letter, Modifier.opacityHigh(), style = theme.text.small, color = ink)
                }
            }
        }
        (0 until (offset + days + 6) / 7).forEach { week ->
            Row(0) {
                (0 until 7).forEach { weekday ->
                    val day = week * 7 + weekday - offset + 1
                    Box(cell, contentAlignment = Alignment.Center) {
                        if (day in 1..days) {
                            val cellDate = LocalDate(month.year, month.month, day)
                            DayCell(day, isPicked = cellDate == date, isToday = cellDate == today) { onPick(cellDate) }
                        }
                    }
                }
            }
        }
    }
}

/** The [day] of a month, filled while [isPicked], ringed while [isToday], calling [onClick]. */
@Composable
private fun DayCell(day: Int, isPicked: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val theme = currentTheme
    val colors = theme.colors
    var modifier = Modifier.size(theme.unit * (DayCellUnits - 1)).clip(CircleShape)
    if (isPicked) modifier = modifier.background(colors.primaryButton)
    if (isToday) modifier = modifier.border(theme.line, colors.primaryButton, CircleShape)
    Box(modifier.pointerClick(onClick), contentAlignment = Alignment.Center) {
        BasicText(day.toString(), style = theme.text.regular, color = { colors.ink })
    }
}

/** A scrolling column of [values] by [label], the [picked] one filled, calling [onPick]. */
@Composable
private fun PickerColumn(values: List<Int>, picked: Int?, label: (Int) -> String, onPick: (Int) -> Unit) {
    val theme = currentTheme
    Column(
        0,
        Modifier.height(theme.unit * PickerHeightUnits).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        values.forEach { value ->
            var modifier = Modifier.width(theme.unit * PickerWidthUnits).clip(CircleShape)
            if (value == picked) modifier = modifier.background(theme.colors.primaryButton)
            BasicText(
                label(value),
                modifier.pointerClick { onPick(value) }.padding(vertical = 0.5),
                style = theme.text.regular.copy(textAlign = TextAlign.Center),
                color = { theme.colors.ink },
            )
        }
    }
}

private fun today() = Clock.System.todayIn(TimeZone.currentSystemDefault())

private fun hourLabelOf(hour: Int) = "${(hour + 11) % 12 + 1} ${if (hour < 12) "AM" else "PM"}"

private fun Int.twoDigits() = toString().padStart(2, '0')

private val WeekdayLetters = listOf("S", "M", "T", "W", "T", "F", "S")

private const val NoonHour = 12
private const val MinuteStep = 5
private const val StepMinutes = 30
private const val SecondsPerDay = 86_400
private const val StepperLabelUnits = 24
private const val MonthLabelUnits = 20
private const val DayCellUnits = 5
private const val PickerHeightUnits = 30
private const val PickerWidthUnits = 10
