package streetlight.model.utils

import streetlight.model.data.TimeWindow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OsmHoursToScheduleTest {

    @Test
    fun `a rule with a second day range after a comma is skipped and the other rules are kept`() {
        val schedule = osmHoursToSchedule("Mo-Th 11:00-22:00, Fr-Sa 11:00-23:00; Su 12:00-20:00")

        val hours = schedule?.default
        assertEquals(listOf(TimeWindow(720, 1200)), hours?.sun, "the plain Sunday rule should be read")
        assertTrue(hours?.mon.isNullOrEmpty(), "the rule holding a second day range should not be half-read")
        assertTrue(hours?.fri.isNullOrEmpty(), "the second day range should not be read as a time")
    }

    @Test
    fun `hours in a form that cannot be read give no schedule`() {
        assertNull(osmHoursToSchedule("Mo-Fr sunset-22:00"))
    }

    @Test
    fun `a day marked off has no hours while the other days keep theirs`() {
        val hours = osmHoursToSchedule("Mo-Fr 09:00-17:00; Sa off")?.default

        assertEquals(listOf(TimeWindow(540, 1020)), hours?.mon)
        assertEquals(emptyList(), hours?.sat)
    }

    @Test
    fun `a day with several windows keeps each of them`() {
        val hours = osmHoursToSchedule("Mo 09:00-12:00,13:00-17:00")?.default

        assertEquals(listOf(TimeWindow(540, 720), TimeWindow(780, 1020)), hours?.mon)
    }
}
