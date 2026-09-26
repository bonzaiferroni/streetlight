package streetlight.server.daemon

import kotlin.test.Test
import kotlin.test.assertEquals

class ParseUtilityTest {

    @Test
    fun `a bracketed note is cut from a title`() {
        assertEquals("Durza: Snake In My Boot", "Durza: Snake In My Boot [SOLD OUT]".withoutBracketNotes())
    }

    @Test
    fun `bracketed notes on both ends of a title are cut`() {
        assertEquals("Durza", "[LOW TICKETS] Durza [SOLD OUT]".withoutBracketNotes())
    }

    @Test
    fun `a title with no brackets is unchanged`() {
        assertEquals("GRiZ (RSVP REQUIRED)", "GRiZ (RSVP REQUIRED)".withoutBracketNotes())
    }

    @Test
    fun `a title of only a bracketed note becomes empty`() {
        assertEquals("", "[SOLD OUT]".withoutBracketNotes())
    }
}
