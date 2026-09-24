package streetlight.model.data

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** The opening hours of a location: its regular hours and the days that override them. */
@Serializable
data class HoursSchedule(
    val default: BusinessHours = BusinessHours(),
    val overrides: List<HoursOverride> = emptyList(),
)

/** Hours that replace the regular ones over a span of dates, once or recurring each year. Empty hours mean closed. */
@Serializable
data class HoursOverride(
    val label: String,            // "Summer Hours", "Christmas Day"
    val start: String,            // "--06-01" recurring, "2026-12-25" one-off
    val end: String,              // "--08-31" or "2026-12-25"
    val recurring: Boolean = false,
    val hours: BusinessHours = BusinessHours(),  // empty = closed
)

/** A span of the day, in minutes from midnight. */
@Serializable
data class TimeWindow(
    val open: Int,  // minutes since midnight: 540 = 09:00
    val close: Int,
) {
    /** True when [minuteOfDay] falls in the window. */
    fun contains(minuteOfDay: Int): Boolean =
        minuteOfDay in open until close

    companion object {
        fun of(open: String, close: String): TimeWindow =
            TimeWindow(toMinutes(open), toMinutes(close))

        private fun toMinutes(hhmm: String): Int {
            val (h, m) = hhmm.split(":").map { it.toInt() }
            return h * 60 + m
        }
    }
}

/** The opening windows of each day of the week. */
@Serializable
data class BusinessHours(
    val mon: List<TimeWindow> = emptyList(),
    val tue: List<TimeWindow> = emptyList(),
    val wed: List<TimeWindow> = emptyList(),
    val thu: List<TimeWindow> = emptyList(),
    val fri: List<TimeWindow> = emptyList(),
    val sat: List<TimeWindow> = emptyList(),
    val sun: List<TimeWindow> = emptyList(),
) {
    /** True when the location is open at [instant] in [zone]. */
    fun isOpen(instant: Instant, zone: TimeZone): Boolean {
        val local = instant.toLocalDateTime(zone)
        val minuteOfDay = local.hour * 60 + local.minute
        return forDay(local.dayOfWeek).any { it.contains(minuteOfDay) }
    }

    private fun forDay(day: DayOfWeek): List<TimeWindow> = when (day) {
        DayOfWeek.MONDAY -> mon
        DayOfWeek.TUESDAY -> tue
        DayOfWeek.WEDNESDAY -> wed
        DayOfWeek.THURSDAY -> thu
        DayOfWeek.FRIDAY -> fri
        DayOfWeek.SATURDAY -> sat
        DayOfWeek.SUNDAY -> sun
    }
}