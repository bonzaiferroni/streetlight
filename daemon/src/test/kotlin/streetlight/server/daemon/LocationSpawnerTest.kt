package streetlight.server.daemon

import kampfire.model.GeoPoint
import streetlight.model.external.Address
import streetlight.model.external.OSMLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationSpawnerTest {

    private val feedPoint = GeoPoint(lng = -104.9876, lat = 39.7392)

    @Test
    fun `a place named by the text away from the feed is distinct`() {
        val bluebird = place(1, "Bluebird Theater", lat = 39.7404, lng = -104.9483)
        assertEquals(bluebird, distinctPlace("Bluebird Theater", feedPoint, listOf(bluebird)))
    }

    @Test
    fun `a place named by the text at the feed location is the feed's own`() {
        val room = place(1, "Tuft Theatre", lat = 39.7393, lng = -104.9877)
        val elsewhere = place(2, "Tuft Theatre", lat = 39.6780, lng = -104.9620)
        assertNull(distinctPlace("Tuft Theatre", feedPoint, listOf(elsewhere, room)), "a room of the feed's venue")
    }

    @Test
    fun `a place beyond the search radius is not distinct`() {
        val far = place(1, "Bluebird Theater", lat = 42.3601, lng = -71.0589)
        assertNull(distinctPlace("Bluebird Theater", feedPoint, listOf(far)))
    }

    @Test
    fun `a place in the mountains within the search radius is distinct`() {
        val redRocks = place(1, "Red Rocks Amphitheatre", lat = 39.6654, lng = -105.2057)
        assertEquals(redRocks, distinctPlace("Red Rocks Amphitheatre", feedPoint, listOf(redRocks)))
    }

    @Test
    fun `a city named by the text is not a place`() {
        val denver = place(1, "Denver", lat = 39.7392, lng = -104.9903, placeRank = 16)
        assertNull(distinctPlace("Denver", feedPoint, listOf(denver)))
    }

    @Test
    fun `a place with another name is not distinct`() {
        val other = place(1, "Ogden Theatre", lat = 39.7404, lng = -104.9750)
        assertNull(distinctPlace("Bluebird Theater", feedPoint, listOf(other)))
    }

    @Test
    fun `no place found is not distinct`() {
        assertNull(distinctPlace("Daniels Hall", feedPoint, emptyList()))
    }

    @Test
    fun `the closest name is chosen, then the nearest place`() {
        val near = place(1, "Bluebird Theater", lat = 39.7404, lng = -104.9483)
        val farther = place(2, "Bluebird Theater", lat = 39.5501, lng = -105.0000)
        val looser = place(3, "Bluebird Theatre", lat = 39.7450, lng = -104.9700)
        assertEquals(near, distinctPlace("Bluebird Theater", feedPoint, listOf(farther, looser, near)))
    }

    private fun place(
        osmId: Long,
        name: String,
        lat: Double,
        lng: Double,
        placeRank: Int = 30,
    ) = OSMLocation(
        placeId = osmId,
        licence = "",
        osmType = "node",
        osmId = osmId,
        lat = lat,
        lon = lng,
        category = "amenity",
        type = "theatre",
        placeRank = placeRank,
        addressType = "amenity",
        name = name,
        displayName = name,
        address = Address(city = "Denver", state = "Colorado"),
        bounds = listOf(lat, lat, lng, lng),
    )
}
