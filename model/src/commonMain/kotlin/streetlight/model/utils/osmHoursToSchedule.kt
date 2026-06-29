package streetlight.model.utils

import kotlinx.datetime.DayOfWeek
import streetlight.model.data.BusinessHours
import streetlight.model.data.HoursOverride
import streetlight.model.data.HoursSchedule
import streetlight.model.data.TimeWindow

private val OSM_DAYS = mapOf(
    "Mo" to DayOfWeek.MONDAY,
    "Tu" to DayOfWeek.TUESDAY,
    "We" to DayOfWeek.WEDNESDAY,
    "Th" to DayOfWeek.THURSDAY,
    "Fr" to DayOfWeek.FRIDAY,
    "Sa" to DayOfWeek.SATURDAY,
    "Su" to DayOfWeek.SUNDAY,
)

private val OSM_MONTHS = mapOf(
    "Jan" to 1, "Feb" to 2, "Mar" to 3, "Apr" to 4,
    "May" to 5, "Jun" to 6, "Jul" to 7, "Aug" to 8,
    "Sep" to 9, "Oct" to 10, "Nov" to 11, "Dec" to 12,
)

private val MONTH_RANGE_REGEX = Regex(
    "^(${OSM_MONTHS.keys.joinToString("|")})" +
            "(?:\\s+(\\d{1,2}))?" +         // optional start day
            "-(${OSM_MONTHS.keys.joinToString("|")})" +
            "(?:\\s+(\\d{1,2}))?\\s*:?\\s*" // optional end day
)

private val SINGLE_DATE_REGEX = Regex(
    "^(${OSM_MONTHS.keys.joinToString("|")})\\s+(\\d{1,2})\\s*:?\\s*"
)

private val ALL_DAYS = DayOfWeek.entries

fun osmHoursToSchedule(raw: String): HoursSchedule? {
    val input = raw.trim()

    if (input == "24/7") {
        val allDay = TimeWindow.of("00:00", "23:59")
        val hours = ALL_DAYS.associateWith { listOf(allDay) }.toBusinessHours()
        return HoursSchedule(default = hours)
    }

    val rules = input.split(";").map { it.trim() }.filter { it.isNotEmpty() }

    val defaultWindows = mutableMapOf<DayOfWeek, MutableList<TimeWindow>>()
    val overrides = mutableListOf<HoursOverride>()

    for (rule in rules) {
        // skip comments
        if (rule.startsWith("\"")) continue

        when {
            rule.startsWith("PH") -> {
                parsePublicHoliday(rule)?.let { overrides.add(it) }
            }
            rule.startsWith("SH") -> {
                parseSchoolHoliday(rule)?.let { overrides.add(it) }
            }
            MONTH_RANGE_REGEX.containsMatchIn(rule) -> {
                parseSeasonalOverride(rule)?.let { overrides.add(it) }
            }
            SINGLE_DATE_REGEX.containsMatchIn(rule) -> {
                parseSingleDateOverride(rule)?.let { overrides.add(it) }
            }
            else -> {
                parseDefaultRule(rule, defaultWindows)
            }
        }
    }

    if (defaultWindows.isEmpty() && overrides.isEmpty()) return null

    return HoursSchedule(
        default = defaultWindows.toBusinessHours(),
        overrides = overrides,
    )
}

private fun parseDefaultRule(
    rule: String,
    dayWindows: MutableMap<DayOfWeek, MutableList<TimeWindow>>,
) {
    val parts = rule.split(" ", limit = 2)
    if (parts.size < 2) return

    val days = parseDays(parts[0]) ?: return
    val windows = parseTimeWindows(parts[1]) ?: return

    for (day in days) {
        dayWindows.getOrPut(day) { mutableListOf() }.addAll(windows)
    }
}

private fun parseSeasonalOverride(rule: String): HoursOverride? {
    val match = MONTH_RANGE_REGEX.find(rule) ?: return null
    val (startMonth, startDay, endMonth, endDay) = match.destructured

    val startMM = OSM_MONTHS[startMonth]?.toString()?.padStart(2, '0') ?: return null
    val endMM = OSM_MONTHS[endMonth]?.toString()?.padStart(2, '0') ?: return null
    val startDD = (startDay.ifEmpty { "01" }).padStart(2, '0')
    val endDD = (endDay.ifEmpty { lastDayOfMonth(endMM) }).padStart(2, '0')

    val remainder = rule.substring(match.range.last + 1).trim()
    val hours = parseRemainingAsBusinessHours(remainder)

    return HoursOverride(
        label = "$startMonth-$endMonth",
        start = "--$startMM-$startDD",
        end = "--$endMM-$endDD",
        recurring = true,
        hours = hours,
    )
}

private fun parseSingleDateOverride(rule: String): HoursOverride? {
    val match = SINGLE_DATE_REGEX.find(rule) ?: return null
    val (month, day) = match.destructured

    val mm = OSM_MONTHS[month]?.toString()?.padStart(2, '0') ?: return null
    val dd = day.padStart(2, '0')

    val remainder = rule.substring(match.range.last + 1).trim()
    val hours = parseRemainingAsBusinessHours(remainder)

    return HoursOverride(
        label = "$month $day",
        start = "--$mm-$dd",
        end = "--$mm-$dd",
        recurring = true,
        hours = hours,
    )
}

private fun parsePublicHoliday(rule: String): HoursOverride? {
    val timePart = rule.removePrefix("PH").trim()
    val hours = if (timePart.isEmpty() || timePart.equals("off", ignoreCase = true)) {
        BusinessHours() // closed
    } else {
        // Apply the time windows to all days so isOpen works regardless of weekday
        val windows = parseTimeWindows(timePart) ?: return null
        ALL_DAYS.associateWith { windows }.toBusinessHours()
    }

    return HoursOverride(
        label = "Public Holidays",
        start = "",  // requires external holiday calendar to resolve
        end = "",
        recurring = true,
        hours = hours,
    )
}

private fun parseSchoolHoliday(rule: String): HoursOverride? {
    val timePart = rule.removePrefix("SH").trim()
    val hours = if (timePart.isEmpty() || timePart.equals("off", ignoreCase = true)) {
        BusinessHours()
    } else {
        parseRemainingAsBusinessHours(timePart)
    }

    return HoursOverride(
        label = "School Holidays",
        start = "",
        end = "",
        recurring = true,
        hours = hours,
    )
}

private fun parseRemainingAsBusinessHours(remainder: String): BusinessHours {
    if (remainder.isEmpty() || remainder.equals("off", ignoreCase = true)) {
        return BusinessHours()
    }

    val dayWindows = mutableMapOf<DayOfWeek, MutableList<TimeWindow>>()
    val subRules = remainder.split(",").map { it.trim() }

    // Check if it's just time windows with no day prefix (e.g. "09:00-17:00")
    val firstPart = subRules[0].split(" ", limit = 2)
    val looksLikeTimeOnly = firstPart[0].contains(":")

    if (looksLikeTimeOnly) {
        val windows = parseTimeWindows(remainder) ?: return BusinessHours()
        ALL_DAYS.forEach { dayWindows[it] = windows.toMutableList() }
    } else {
        for (sub in subRules) {
            val parts = sub.split(" ", limit = 2)
            if (parts.size < 2) continue
            val days = parseDays(parts[0]) ?: continue
            val windows = parseTimeWindows(parts[1]) ?: continue
            for (day in days) {
                dayWindows.getOrPut(day) { mutableListOf() }.addAll(windows)
            }
        }
    }

    return dayWindows.toBusinessHours()
}

private fun parseDays(raw: String): List<DayOfWeek>? {
    val days = mutableListOf<DayOfWeek>()

    for (segment in raw.split(",")) {
        val trimmed = segment.trim()
        if ("-" in trimmed) {
            val (startStr, endStr) = trimmed.split("-", limit = 2)
            val start = OSM_DAYS[startStr] ?: return null
            val end = OSM_DAYS[endStr] ?: return null
            var current = start
            while (true) {
                days.add(current)
                if (current == end) break
                current = DayOfWeek.entries[(current.ordinal + 1) % 7]
            }
        } else {
            days.add(OSM_DAYS[trimmed] ?: return null)
        }
    }

    return days.takeIf { it.isNotEmpty() }
}

private fun parseTimeWindows(raw: String): List<TimeWindow>? {
    if (raw.trim().equals("off", ignoreCase = true)) return emptyList()

    return raw.split(",").mapNotNull { segment ->
        val (open, close) = segment.trim().split("-", limit = 2)
            .takeIf { it.size == 2 } ?: return@mapNotNull null
        TimeWindow.of(open.trim(), close.trim())
    }.takeIf { it.isNotEmpty() }
}

private fun lastDayOfMonth(mm: String): String = when (mm) {
    "02" -> "28"
    "04", "06", "09", "11" -> "30"
    else -> "31"
}

private fun Map<DayOfWeek, List<TimeWindow>>.toBusinessHours() = BusinessHours(
    mon = this[DayOfWeek.MONDAY].orEmpty(),
    tue = this[DayOfWeek.TUESDAY].orEmpty(),
    wed = this[DayOfWeek.WEDNESDAY].orEmpty(),
    thu = this[DayOfWeek.THURSDAY].orEmpty(),
    fri = this[DayOfWeek.FRIDAY].orEmpty(),
    sat = this[DayOfWeek.SATURDAY].orEmpty(),
    sun = this[DayOfWeek.SUNDAY].orEmpty(),
)