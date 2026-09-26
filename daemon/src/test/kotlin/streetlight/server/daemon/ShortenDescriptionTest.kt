package streetlight.server.daemon

import kampfire.model.toUrl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShortenDescriptionTest {

    private val url = "https://venue.example/events/show".toUrl()
    private val sentence = "The band plays a long set of songs from every album they have made so far. "

    @Test
    fun `a description within the limit is kept whole with no link`() {
        val text = "A short evening of jazz."
        assertEquals(text, shortenDescription(text, url))
    }

    @Test
    fun `a long description keeps the whole paragraphs that fit and links to the page`() {
        val paragraph = sentence.repeat(4).trim()
        val text = List(6) { paragraph }.joinToString("\n\n")

        val shortened = shortenDescription(text, url)

        val kept = shortened.substringBefore("\n\n[Read more]")
        assertTrue(kept.length <= 1000, "the kept text should fit the limit")
        assertTrue(kept.split("\n\n").all { it == paragraph }, "only whole paragraphs should be kept")
        assertTrue(shortened.endsWith("[Read more]($url)"), "a shortened description should link to the page")
    }

    @Test
    fun `a first paragraph over the limit is cut at a sentence`() {
        val text = sentence.repeat(30).trim()

        val kept = shortenDescription(text, url).substringBefore("\n\n[Read more]")

        assertTrue(kept.length <= 1000, "the kept text should fit the limit")
        assertTrue(kept.endsWith("."), "the cut should fall at the end of a sentence")
    }

    @Test
    fun `a shortened description with no page has no link`() {
        val text = sentence.repeat(30).trim()
        assertTrue("Read more" !in shortenDescription(text, null))
    }

    @Test
    fun `text with no sentence to cut at is cut at a word`() {
        val text = "word ".repeat(400).trim()

        val kept = shortenDescription(text, null)

        assertTrue(kept.length <= 1001, "the kept text should fit the limit with its ellipsis")
        assertTrue(kept.endsWith("word…"), "the cut should fall after a whole word")
    }
}
