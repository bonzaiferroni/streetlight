@file:Suppress("SpellCheckingInspection")

package streetlight.server.daemon.agent

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

fun parseLocalDateTime(rawText: String, timeZoneId: String?): LocalDateTime? {
    val text = rawText.normalizeSpaces()
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

private val leadingIsoDate = Regex("""\d{4}-\d{2}-\d{2}""")

private val EPOCH_SECONDS_RANGE = 946_684_800L..4_102_444_800L  // 2000-01-01 to 2100-01-01

fun parseLocalDateTimeFromFormat(text: String): LocalDateTime? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null

    runCatching { LocalDateTime.parse(trimmed.replaceFirst(' ', 'T')) }
        .getOrNull()
        ?.let { return it }

    val isoDate = leadingIsoDate.matchAt(trimmed, 0) ?: return null
    val date = runCatching { LocalDate.parse(isoDate.value) }.getOrNull() ?: return null
    val time = parseTimeFromText(trimmed.substring(isoDate.range.last + 1)) ?: return null
    return date.atTime(time)
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

private val rangeSeparator = Regex("""\s*(?:[-–—]|to|until|till|through|thru)\s*""")

/** Replaces each run of whitespace, including Unicode spaces, with a single space. */
private fun String.normalizeSpaces() = replace(unicodeSpace, " ")

private val unicodeSpace = Regex("""[\s\p{Z}]+""")

private val yearPattern = Regex("""\b(\d{4})\b""")
private val dayPattern = Regex("""\b(\d{1,2})(?:st|nd|rd|th)?\b""", RegexOption.IGNORE_CASE)
private val wordPattern = Regex("""[a-z]+""")

/** Whether this text names a month, such as "Sep" or "September". */
internal fun String.hasMonthName(): Boolean = wordPattern.findAll(normalizeSpaces().lowercase()).any { it.value in monthNames }

/** A span of the day read from text, its [end] given only when the text states one. */
data class TimeRange(val start: LocalTime, val end: LocalTime?)

/** A time of day as written, its [meridiem] `'a'` or `'p'` when stated. */
private data class TimeToken(val hour: Int, val minute: Int, val meridiem: Char?, val isZeroPadded: Boolean, val range: IntRange) {
    /** Whether this token could be either half of the day. */
    val isAmbiguous get() = meridiem == null && hour in 1..12 && !isZeroPadded

    fun toLocalTime(meridiem: Char? = this.meridiem): LocalTime {
        val adjusted = when {
            meridiem == 'p' && hour < 12 -> hour + 12
            meridiem == 'a' && hour == 12 -> 0
            else -> hour
        }
        return LocalTime(adjusted, minute)
    }

    /** The time this token states, or null when it is ambiguous in text where [hasMeridiem]. */
    fun resolve(hasMeridiem: Boolean): LocalTime? = if (isAmbiguous && hasMeridiem) null else toLocalTime()
}

private fun Char.flipped() = if (this == 'a') 'p' else 'a'

private fun MatchResult.toTimeToken(): TimeToken? {
    val hourText = groupValues[1].ifEmpty { groupValues[4] }
    val hour = hourText.toIntOrNull() ?: return null
    val minute = groupValues[2].ifEmpty { groupValues[5] }.ifEmpty { "0" }.toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    val meridiem = groupValues[3].firstOrNull()
    return TimeToken(hour, minute, meridiem, hourText.length == 2 && hourText[0] == '0', range)
}

/**
 * Reads the first time of day in [text], with the end of the range it opens.
 *
 * A time without a meridiem borrows one from the other side of its range, flipped when it would put the start
 * after the end. Otherwise it is read on the 24-hour clock, unless it could be either half of the day and the text
 * states a meridiem elsewhere, which leaves it unread.
 */
fun parseTimeRange(text: String): TimeRange? {
    val lower = text.normalizeSpaces().lowercase()
    val tokens = timePattern.findAll(lower).mapNotNull { it.toTimeToken() }.toList()
    val first = tokens.firstOrNull() ?: return null
    val second = tokens.getOrNull(1)
        ?.takeIf { rangeSeparator.matches(lower.substring(first.range.last + 1, it.range.first)) }
    val hasMeridiem = tokens.any { it.meridiem != null }

    if (second == null) return first.resolve(hasMeridiem)?.let { TimeRange(it, null) }

    return when {
        first.isAmbiguous && second.meridiem != null -> {
            val end = second.toLocalTime()
            val start = first.toLocalTime(second.meridiem).takeIf { it <= end }
                ?: first.toLocalTime(second.meridiem.flipped())
            TimeRange(start, end)
        }
        second.isAmbiguous && first.meridiem != null -> {
            val start = first.toLocalTime()
            val end = second.toLocalTime(first.meridiem).takeIf { it >= start }
                ?: second.toLocalTime(first.meridiem.flipped())
            TimeRange(start, end)
        }
        else -> TimeRange(first.resolve(hasMeridiem) ?: return null, second.resolve(hasMeridiem))
    }
}

/** The start of the first time of day in [text], read by [parseTimeRange]. */
fun parseTimeFromText(text: String): LocalTime? = parseTimeRange(text)?.start

fun parseDateFromText(text: String, now: Instant, zone: TimeZone): LocalDate? {
    val lower = text.normalizeSpaces().lowercase()
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