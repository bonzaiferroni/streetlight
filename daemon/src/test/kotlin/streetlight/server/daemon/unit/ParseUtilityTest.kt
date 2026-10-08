package streetlight.server.daemon.unit

import kotlin.test.Test
import kotlin.test.assertEquals
import streetlight.server.daemon.crawler.cleanLocationName
import streetlight.server.daemon.crawler.withoutBracketNotes

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

    @Test
    fun `a venue after an at sign is cut from a title`() {
        assertEquals("Doja Cat", "Doja Cat @ Ball Arena".cleanLocationName("Ball Arena", threshold = 0.6))
    }

    @Test
    fun `a venue written with an article and another spelling is cut from a title`() {
        assertEquals("Iron & Wine", "Iron & Wine | The Ogden Theater".cleanLocationName("Ogden Theatre", threshold = 0.6))
    }

    @Test
    fun `a venue and a city chained at the end are both cut from a title`() {
        assertEquals(
            "Michelle Branch",
            "Michelle Branch | Boulder Theater — Boulder".cleanLocationName("Boulder Theater", "Boulder", threshold = 0.6),
        )
    }

    @Test
    fun `a separator inside a title that names no place is kept`() {
        val title = "CLASS | FLOW & RESTORATIVE YOGA"
        assertEquals(title, title.cleanLocationName("St. Vrain Cidery", "Longmont", threshold = 0.6))
    }

    @Test
    fun `a segment holding the venue among other words is kept`() {
        val title = "Candlelight - Queen vs The Beatles at First United Methodist"
        assertEquals(title, title.cleanLocationName("First United Methodist Church", threshold = 0.6))
    }

    @Test
    fun `a title that is only the venue is kept`() {
        assertEquals("Ball Arena", "Ball Arena".cleanLocationName("Ball Arena", threshold = 0.6))
    }

    @Test
    fun `a missing place leaves a title unchanged`() {
        assertEquals("Doja Cat @ Ball Arena", "Doja Cat @ Ball Arena".cleanLocationName(null, threshold = 0.6))
    }
}
