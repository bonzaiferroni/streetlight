package streetlight.server.daemon.unit

import kotlinx.datetime.LocalDate
import streetlight.server.daemon.agent.parseDateFromText
import streetlight.server.daemon.agent.parseLocalDateTime
import streetlight.server.daemon.agent.parseLocalDateTimeFromText
import streetlight.server.daemon.agent.TimeRange
import streetlight.server.daemon.agent.parseTimeFromText
import streetlight.server.daemon.agent.parseTimeRange
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ParseTimeUtilityTest {

    private val zone = TimeZone.of("America/Denver")
    private val now = Instant.parse("2026-09-20T18:00:00Z")

    @Test
    fun `a time with a narrow no-break space before PM is read as evening`() {
        assertEquals(LocalTime(20, 0), parseTimeFromText("8:00 PM"))
    }

    @Test
    fun `a time with a no-break space before pm is read as evening`() {
        assertEquals(LocalTime(23, 0), parseTimeFromText("11:00 pm"))
    }

    @Test
    fun `a date and time separated by a narrow no-break space are read together`() {
        assertEquals(
            LocalDateTime(2026, 9, 26, 20, 0),
            parseLocalDateTime("Sep 26, 2026 8:00 PM", zone.id),
        )
    }

    @Test
    fun `a day-first date with a dotted weekday is read with its time`() {
        assertEquals(
            LocalDateTime(2026, 9, 25, 19, 30),
            parseLocalDateTimeFromText("Fri. 25 Sep, 2026 7:30 PM", now, zone),
        )
    }

    @Test
    fun `a year-less date is read as the nearest such date`() {
        assertEquals(LocalDate(2026, 9, 25), parseDateFromText("Sep 25", now, zone))
    }

    @Test
    fun `a date with no time has no start`() {
        assertNull(parseLocalDateTimeFromText("Sep 25", now, zone), "a start needs a time")
    }

    @Test
    fun `a month with no day has no date`() {
        assertNull(parseDateFromText("Mar | 8:00 PM 11:00 PM", now, zone))
    }

    @Test
    fun `an ISO date and time joined by a space are read together`() {
        assertEquals(LocalDateTime(2026, 10, 16, 20, 0), parseLocalDateTime("2026-10-16 20:00", zone.id))
    }

    @Test
    fun `an ISO date with no time has no start`() {
        assertNull(parseLocalDateTime("2026-10-16", zone.id), "a start needs a time")
    }

    @Test
    fun `a range start without a meridiem takes the meridiem of its end`() {
        assertEquals(
            LocalDateTime(2026, 10, 9, 17, 45),
            parseLocalDateTime("Friday, October 9 · 5:45–7:15 p.m.", zone.id),
        )
        assertEquals(TimeRange(LocalTime(17, 45), LocalTime(19, 15)), parseTimeRange("5:45–7:15 p.m."))
    }

    @Test
    fun `a range start that would follow its end takes the other meridiem`() {
        assertEquals(TimeRange(LocalTime(11, 30), LocalTime(12, 30)), parseTimeRange("11:30 - 12:30 pm"))
    }

    @Test
    fun `a range end without a meridiem that would precede its start crosses into the next half of the day`() {
        assertEquals(TimeRange(LocalTime(21, 0), LocalTime(1, 0)), parseTimeRange("9:00 PM to 1:00"))
    }

    @Test
    fun `a range with both meridiems keeps each`() {
        assertEquals(TimeRange(LocalTime(21, 0), LocalTime(1, 0)), parseTimeRange("9 pm until 1 am"))
    }

    @Test
    fun `times separated by space alone are not a range`() {
        assertEquals(TimeRange(LocalTime(20, 0), null), parseTimeRange("8:00 PM 11:00 PM"))
    }

    @Test
    fun `a time without a meridiem in text that states none is read on the 24-hour clock`() {
        assertEquals(TimeRange(LocalTime(5, 45), LocalTime(7, 15)), parseTimeRange("5:45–7:15"))
        assertEquals(LocalTime(19, 30), parseTimeFromText("19:30"))
    }

    @Test
    fun `a time without a meridiem in text that states one elsewhere is unread`() {
        assertNull(parseTimeFromText("Doors 6:30, show 7:30 p.m."), "6:30 could be either half of the day")
    }

    @Test
    fun `a zero-padded or afternoon hour is read on the 24-hour clock beside a meridiem`() {
        assertEquals(LocalTime(9, 0), parseTimeFromText("09:00, ends 5 pm"))
        assertEquals(LocalTime(18, 30), parseTimeFromText("18:30, show 7:30 pm"))
    }
}
