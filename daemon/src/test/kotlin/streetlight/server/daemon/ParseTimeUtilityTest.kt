package streetlight.server.daemon

import kotlinx.datetime.LocalDate
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
}
