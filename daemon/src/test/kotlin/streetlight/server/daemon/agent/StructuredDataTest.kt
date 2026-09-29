package streetlight.server.daemon.agent

import com.fleeksoft.ksoup.Ksoup
import kampfire.model.toUrl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StructuredDataTest {

    private fun page(vararg blocks: String) = Ksoup.parse(
        blocks.joinToString("") { """<script type="application/ld+json">$it</script>""" },
        "https://venue.example/events/jazz-night",
    )

    private val jazzNight = """
        {
            "@context": "https://schema.org",
            "@type": "MusicEvent",
            "name": "Jazz Night",
            "url": "https://venue.example/events/jazz-night",
            "startDate": "2026-10-16T20:00:00-06:00",
            "endDate": "2026-10-16T23:00:00-06:00",
            "image": ["https://venue.example/jazz.jpg"],
            "eventStatus": "https://schema.org/EventScheduled",
            "offers": { "@type": "Offer", "price": "15", "priceCurrency": "USD" },
            "location": {
                "@type": "Place",
                "name": "The Venue",
                "address": {
                    "@type": "PostalAddress",
                    "streetAddress": "2736 Welton Street",
                    "addressLocality": "Denver",
                    "addressRegion": "CO"
                }
            }
        }
    """

    private val pageUrl = "https://venue.example/events/jazz-night".toUrl()

    @Test
    fun `an event page's own event is read with its time as the page states it`() {
        val schema = pageLdEvent(page(jazzNight).readLdEvents(), pageUrl)?.toEventSchema()

        assertEquals("Jazz Night", schema?.name)
        assertEquals("2026-10-16", schema?.date)
        assertEquals("20:00", schema?.startTime)
        assertEquals("23:00", schema?.endTime)
        assertEquals("$15", schema?.cost)
        assertEquals("https://venue.example/jazz.jpg", schema?.imageUrl)
        assertEquals("The Venue", schema?.locationName)
        assertEquals("2736 Welton Street", schema?.locationAddress)
        assertEquals("Denver", schema?.locationCity)
    }

    @Test
    fun `the page's event is picked by its url from among several`() {
        val other = jazzNight.replace("Jazz Night", "Blues Night").replace("jazz-night", "blues-night")

        val event = pageLdEvent(page(other, jazzNight).readLdEvents(), pageUrl)

        assertEquals("Jazz Night", event?.name)
    }

    @Test
    fun `no event is picked from several when none is the page's`() {
        val first = jazzNight.replace("jazz-night", "blues-night")
        val second = jazzNight.replace("jazz-night", "soul-night")

        assertNull(pageLdEvent(page(first, second).readLdEvents(), pageUrl))
    }

    @Test
    fun `an event nested in a graph is found`() {
        val graph = """{ "@context": "https://schema.org", "@graph": [ { "@type": "WebPage", "name": "Events" }, $jazzNight ] }"""

        assertEquals(listOf("Jazz Night"), page(graph).readLdEvents().map { it.name })
    }

    @Test
    fun `a cancelled event is called off`() {
        val cancelled = jazzNight.replace("EventScheduled", "EventCancelled")

        assertTrue(page(cancelled).readLdEvents().single().isCalledOff)
    }

    @Test
    fun `an event with a start date but no time gives no schema`() {
        val dateOnly = jazzNight.replace("2026-10-16T20:00:00-06:00", "2026-10-16")

        assertNull(page(dateOnly).readLdEvents().single().toEventSchema())
    }

    @Test
    fun `an end on a later date is not taken as the end time`() {
        val overnight = jazzNight.replace("2026-10-16T23:00:00-06:00", "2026-10-17T01:00:00-06:00")

        assertNull(page(overnight).readLdEvents().single().toEventSchema()?.endTime)
    }

    @Test
    fun `a price of zero is free`() {
        val free = jazzNight.replace("\"price\": \"15\"", "\"price\": 0")

        assertEquals("Free", page(free).readLdEvents().single().toEventSchema()?.cost)
    }

    @Test
    fun `a price in another currency is left out`() {
        val euros = jazzNight.replace("USD", "EUR")

        assertNull(page(euros).readLdEvents().single().toEventSchema()?.cost)
    }

    @Test
    fun `a plain description becomes paragraphs`() {
        val described = jazzNight.replace("\"name\": \"Jazz Night\",", "\"name\": \"Jazz Night\", \"description\": \"Doors at 7.\\n\\nTwo sets.\\nNo cover.\",")

        assertEquals("<p>Doors at 7.</p><p>Two sets.<br>No cover.</p>", page(described).readLdEvents().single().description)
    }

    @Test
    fun `a description holding html is kept as it is`() {
        val described = jazzNight.replace("\"name\": \"Jazz Night\",", "\"name\": \"Jazz Night\", \"description\": \"<p>Doors at <b>7</b>.</p>\",")

        assertEquals("<p>Doors at <b>7</b>.</p>", page(described).readLdEvents().single().description)
    }

    @Test
    fun `an event's location is not read as a place of its own`() {
        assertEquals(emptyList(), page(jazzNight).readLdPlaces())
    }

    @Test
    fun `a business with an address is read as a place`() {
        val bar = """
            {
                "@context": "https://schema.org",
                "@type": "BarOrPub",
                "name": "Bar 404",
                "url": "https://bar404.example",
                "telephone": "303-555-0100",
                "email": "mailto:hello@bar404.example",
                "address": {
                    "@type": "PostalAddress",
                    "streetAddress": "404 Main Street",
                    "addressLocality": "Denver",
                    "addressRegion": "CO",
                    "postalCode": "80202",
                    "addressCountry": { "@type": "Country", "name": "US" }
                }
            }
        """

        val schema = page(bar).readLdPlaces().single().toLocationSchema()

        assertEquals("Bar 404", schema?.name)
        assertEquals("404 Main Street", schema?.address)
        assertEquals("hello@bar404.example", schema?.email)
        assertEquals("US", schema?.country)
    }

    @Test
    fun `a time element is read by its datetime`() {
        val doc = Ksoup.parse("""<time datetime="2026-10-16T20:00">Friday at 8</time>""")

        assertEquals("2026-10-16T20:00", doc.selectFirst("time").plainText())
    }

    @Test
    fun `a time element whose datetime is a date alone is read by its text`() {
        val doc = Ksoup.parse("""<time datetime="2026-10-08">9:00 PM</time>""")

        assertEquals("9:00 PM", doc.selectFirst("time").plainText())
    }
}
