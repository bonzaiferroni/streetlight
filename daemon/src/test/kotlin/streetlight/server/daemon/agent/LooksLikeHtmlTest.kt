package streetlight.server.daemon.agent

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LooksLikeHtmlTest {

    @Test
    fun `a page opening with a comment is html`() {
        assertTrue("<!-- This page is cached by the Hummingbird Performance plugin -->\n<!DOCTYPE html><html></html>".looksLikeHtml())
    }

    @Test
    fun `a page opening with a doctype is html`() {
        assertTrue("<!DOCTYPE html><html></html>".looksLikeHtml())
    }

    @Test
    fun `json is not html`() {
        assertFalse("""{"events": []}""".looksLikeHtml())
    }
}
