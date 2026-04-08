package streetlight.model.utils

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

fun LocalDateTime.toEpochSeconds() = toInstant(TimeZone.currentSystemDefault()).epochSeconds
fun Long.toLocalDateTime() = Instant.fromEpochSeconds(this)
    .toLocalDateTime(TimeZone.currentSystemDefault())

fun Instant.toLocalEpochSeconds() = toLocalDateTime(TimeZone.currentSystemDefault())
    .toEpochSeconds()

fun Instant.toLocalDateTime() = toLocalDateTime(TimeZone.currentSystemDefault())

@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalDateTime.toFormatString(pattern: String) = this.format(
    LocalDateTime.Format { byUnicodePattern(pattern) }
)

fun tomorrowNoon(): Instant {
    val zone = TimeZone.currentSystemDefault()

    val now = Clock.System.now()
    val today = now.toLocalDateTime(zone).date
    val tomorrow = today.plus(DatePeriod(days = 1))

    return LocalDateTime(
        date = tomorrow,
        time = LocalTime(12, 0)
    ).toInstant(zone)
}