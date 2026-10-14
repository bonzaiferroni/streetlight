package streetlight.model.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CentsTest {

    @Test
    fun `whole and decimal amounts read as cents`() {
        assertEquals(1250, "12.50".toCentsOrNull())
        assertEquals(1250, "12.5".toCentsOrNull())
        assertEquals(1200, "12".toCentsOrNull())
        assertEquals(1200, "12.".toCentsOrNull())
        assertEquals(50, ".5".toCentsOrNull())
        assertEquals(1250, " 12.50 ".toCentsOrNull())
    }

    @Test
    fun `text that is not an amount reads as null`() {
        assertNull("".toCentsOrNull())
        assertNull(".".toCentsOrNull())
        assertNull("12.505".toCentsOrNull())
        assertNull("-5".toCentsOrNull())
        assertNull("$12".toCentsOrNull())
        assertNull("1.2.3".toCentsOrNull())
    }

    @Test
    fun `cents read back as a label with two decimal places`() {
        assertEquals("12.50", 1250.toCentsLabel())
        assertEquals("0.05", 5.toCentsLabel())
    }
}
