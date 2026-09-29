package streetlight.server.daemon.crawler

import kotlin.test.Test
import kotlin.test.assertEquals

class AddressUnitTest {

    @Test
    fun `a unit after a hash is left out`() {
        assertEquals("2501 Dallas Street", withoutUnit("2501 Dallas Street #100"))
    }

    @Test
    fun `a named unit after a comma is left out`() {
        assertEquals("2501 Dallas Street", withoutUnit("2501 Dallas Street, Unit 148"))
    }

    @Test
    fun `an abbreviated suite is left out`() {
        assertEquals("500 16th St", withoutUnit("500 16th St Ste. 200"))
    }

    @Test
    fun `a street named like a unit is kept`() {
        assertEquals("100 Suite Rd", withoutUnit("100 Suite Rd"))
    }

    @Test
    fun `an address without a unit is unchanged`() {
        assertEquals("2430 S Havana St", withoutUnit("2430 S Havana St"))
    }
}
