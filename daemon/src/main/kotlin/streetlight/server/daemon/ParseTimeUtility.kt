@file:Suppress("SpellCheckingInspection")

package streetlight.server.daemon

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.parse
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

fun parseLocalDateTime(text: String, timeZoneId: String?): LocalDateTime? {
    val zone = timeZoneId
        ?.let { id -> runCatching { TimeZone.of(id) }.getOrNull() }
        ?: return null

    parseInstantFromFormat(text)?.let { instant ->
        return instant.toLocalDateTime(zone)
    }

    parseLocalDateTimeFromFormat(text)?.let { return it }

    return parseLocalDateTimeFromText(text, Clock.System.now(), zone)
}

fun parseInstantFromFormat(text: String): Instant? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null

    val formats = listOf(
        DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET,
        DateTimeComponents.Formats.RFC_1123,
    )

    formats.forEach { format ->
        runCatching { Instant.parse(trimmed, format) }
            .getOrNull()
            ?.let { return it }
    }

    return trimmed.toLongOrNull()
        ?.takeIf { it in EPOCH_SECONDS_RANGE }
        ?.let { Instant.fromEpochSeconds(it) }
}

private val EPOCH_SECONDS_RANGE = 946_684_800L..4_102_444_800L  // 2000-01-01 to 2100-01-01

fun parseLocalDateTimeFromFormat(text: String): LocalDateTime? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null

    runCatching { LocalDateTime.parse(trimmed) }
        .getOrNull()
        ?.let { return it }

    runCatching { LocalDate.parse(trimmed) }
        .getOrNull()
        ?.let { return it.atTime(0, 0) }

    return null
}

private val monthNames = mapOf(
    "january" to 1, "february" to 2, "march" to 3, "april" to 4,
    "may" to 5, "june" to 6, "july" to 7, "august" to 8,
    "september" to 9, "october" to 10, "november" to 11, "december" to 12,
    "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
    "jun" to 6, "jul" to 7, "aug" to 8,
    "sept" to 9, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12,
)

private val weekdayNames = mapOf(
    "monday" to DayOfWeek.MONDAY, "tuesday" to DayOfWeek.TUESDAY,
    "wednesday" to DayOfWeek.WEDNESDAY, "thursday" to DayOfWeek.THURSDAY,
    "friday" to DayOfWeek.FRIDAY, "saturday" to DayOfWeek.SATURDAY,
    "sunday" to DayOfWeek.SUNDAY,
    "mon" to DayOfWeek.MONDAY, "tue" to DayOfWeek.TUESDAY, "tues" to DayOfWeek.TUESDAY,
    "wed" to DayOfWeek.WEDNESDAY, "thu" to DayOfWeek.THURSDAY, "thur" to DayOfWeek.THURSDAY,
    "thurs" to DayOfWeek.THURSDAY, "fri" to DayOfWeek.FRIDAY, "sat" to DayOfWeek.SATURDAY,
    "sun" to DayOfWeek.SUNDAY,
)

private val timePattern = Regex(
    """\b(\d{1,2})(?::(\d{2}))?\s*([ap])\.?\s?m\.?|\b(\d{1,2}):(\d{2})\b""",
    RegexOption.IGNORE_CASE,
)

private val yearPattern = Regex("""\b(\d{4})\b""")
private val dayPattern = Regex("""\b(\d{1,2})(?:st|nd|rd|th)?\b""", RegexOption.IGNORE_CASE)
private val wordPattern = Regex("""[a-z]+""")

fun parseTimeFromText(text: String): LocalTime? =
    timePattern.find(text.lowercase())?.let { match ->
        val meridiem = match.groupValues[3].takeIf { it.isNotEmpty() }
        val hour = (match.groupValues[1].takeIf { it.isNotEmpty() }
            ?: match.groupValues[4]).toIntOrNull() ?: return null
        val minute = (match.groupValues[2].takeIf { it.isNotEmpty() }
            ?: match.groupValues[5].takeIf { it.isNotEmpty() }
            ?: "0").toIntOrNull() ?: return null

        val adjusted = when {
            meridiem == "p" && hour < 12 -> hour + 12
            meridiem == "a" && hour == 12 -> 0
            else -> hour
        }
        if (adjusted !in 0..23 || minute !in 0..59) return null
        LocalTime(adjusted, minute)
    }

fun parseDateFromText(text: String, now: Instant, zone: TimeZone): LocalDate? {
    val lower = text.lowercase()
    val withoutTime = timePattern.replace(lower, " ")

    val words = wordPattern.findAll(withoutTime).map { it.value }.toList()
    val month = words.firstNotNullOfOrNull { monthNames[it] } ?: return null
    val weekday = words.firstNotNullOfOrNull { weekdayNames[it] }

    val yearMatch = yearPattern.find(withoutTime)
    val statedYear = yearMatch?.groupValues?.get(1)?.toIntOrNull()

    val withoutYear = yearMatch?.let { withoutTime.removeRange(it.range) } ?: withoutTime
    val day = dayPattern.find(withoutYear)?.groupValues?.get(1)?.toIntOrNull() ?: return null
    if (day !in 1..31) return null

    statedYear?.let { year ->
        return runCatching { LocalDate(year, month, day) }.getOrNull()
    }

    val today = now.toLocalDateTime(zone).date
    val candidates = (today.year - 1..today.year + 1)
        .mapNotNull { year -> runCatching { LocalDate(year, month, day) }.getOrNull() }

    val matching = weekday
        ?.let { named -> candidates.filter { it.dayOfWeek == named } }
        ?.takeIf { it.isNotEmpty() }
        ?: candidates

    return matching.minByOrNull { abs(today.daysUntil(it)) }
}

fun parseLocalDateTimeFromText(text: String, now: Instant, zone: TimeZone): LocalDateTime? =
    parseDateFromText(text, now, zone)?.atTime(parseTimeFromText(text) ?: return null)