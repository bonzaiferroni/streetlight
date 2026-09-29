package streetlight.server.daemon.crawler

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CostTest {

    @Test
    fun `a free event costs nothing`() {
        assertEquals(0f, costOf("Free"))
    }

    @Test
    fun `a single price is read in dollars`() {
        assertEquals(15f, costOf("$15"))
    }

    @Test
    fun `the lowest of several prices is read`() {
        assertEquals(15f, costOf("$15 advance / $20 at the door"))
    }

    @Test
    fun `cents are kept`() {
        assertEquals(12.5f, costOf("Tickets: $12.50"))
    }

    @Test
    fun `a price wins over the word free`() {
        assertEquals(10f, costOf("Free for members, $10 general admission"))
    }

    @Test
    fun `text with no price and no free has no cost`() {
        assertNull(costOf("Doors at 7"))
    }
}
