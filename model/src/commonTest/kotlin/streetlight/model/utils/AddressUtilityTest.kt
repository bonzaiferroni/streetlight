package streetlight.model.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class AddressUtilityTest {

    @Test
    fun `a direction and suffix are spelled out`() {
        assertEquals("14200 East Alameda Avenue", "14200 E Alameda Ave".expandAddress())
    }

    @Test
    fun `abbreviations written with periods are spelled out`() {
        assertEquals("4335 West 44th Avenue", "4335 W. 44th Ave.".expandAddress())
    }

    @Test
    fun `a spelled out address is unchanged`() {
        assertEquals("14200 East Alameda Avenue", "14200 East Alameda Avenue".expandAddress())
    }

    @Test
    fun `a direction after the street is spelled out`() {
        assertEquals("100 Main Street Northwest", "100 Main St NW".expandAddress())
    }

    @Test
    fun `a saint at the front of the street name is kept`() {
        assertEquals("100 St. Paul Street", "100 St. Paul Street".expandAddress())
    }

    @Test
    fun `a street named by a single letter keeps its name`() {
        assertEquals("100 E Street", "100 E St".expandAddress())
    }

    @Test
    fun `a unit is kept as written after the expanded street`() {
        assertEquals("2501 Dallas Street Unit 148", "2501 Dallas St Unit 148".expandAddress())
    }

    @Test
    fun `the city and state after the street line are kept as written`() {
        assertEquals("1338 1st Street, Denver, CO", "1338 1st St, Denver, CO".expandAddress())
    }
}
