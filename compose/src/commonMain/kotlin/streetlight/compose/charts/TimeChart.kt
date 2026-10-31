package streetlight.compose.charts

import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/** Generates [AxisTick]s evenly spaced across the time window from [earliest] to [latest]. */
fun generateTimeAxis(earliest: Instant, latest: Instant = Clock.System.now()): List<AxisTick> {
    val now = Clock.System.now()
    val span = (latest - earliest).takeIf { it.isPositive() } ?: 1.hours
    val interval = when {
        span > 21.days -> 7.days
        span > 10.days -> 3.days
        span > 2.days -> 1.days
        else -> 6.hours
    }
    val tz = TimeZone.currentSystemDefault()
    val dayStart = earliest.toLocalDateTime(tz).date.atStartOfDayIn(tz)
    val timeStart = dayStart + interval * (((earliest - dayStart) / interval).toInt() + 1)
    val intervalCount = ((span / interval).toInt()).coerceAtLeast(1)
    val currentYear = now.toLocalDateTime(tz).year

    return (0 until intervalCount).map { i ->
        val time = timeStart + interval * i
        val localTime = time.toLocalDateTime(tz)
        val year = localTime.year.toString()
        val date = "${localTime.monthNumber}/${localTime.dayOfMonth}"
        val day = localTime.dayOfWeek.toString().take(3)
        val label = when {
            localTime.year != currentYear -> "$day,\n$date, $year"
            time < now - 7.days -> "$day,\n$date"
            localTime.hour == 0 && localTime.minute == 0 -> day
            else -> "${localTime.hour}:${localTime.minute.toString().padStart(2, '0')}"
        }

        AxisTick(time.toEpochMilliseconds().toDouble(), label)
    }
}
