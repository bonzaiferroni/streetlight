package streetlight.server.daemon.crawler

import kampfire.utils.fuzzyMatches
import kampfire.utils.similarity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FuzzyMatchTest {

    @Test
    fun `a title matches the same title with a supporting act added`() {
        assertTrue("Blonde Redhead".fuzzyMatches("Blonde Redhead with Allison Lorenzen"))
    }

    @Test
    fun `a title matches the same title with a different spelling of one word`() {
        assertTrue("The Oriental Theater".fuzzyMatches("Oriental Theatre"))
    }

    @Test
    fun `a title matches the same title in a different case`() {
        assertTrue("Yoga Mayhem".fuzzyMatches("YOGA MAYHEM"))
    }

    @Test
    fun `a title matches the same title with different punctuation between acts`() {
        assertTrue("Poison Politix + One Time Crime + Nadiya Band".fuzzyMatches("Poison Politix, One Time Crime & Nadiya Band"))
    }

    @Test
    fun `titles sharing only a common word do not match`() {
        assertFalse("Comedy Night".fuzzyMatches("Comedy Showcase"))
    }

    @Test
    fun `different shows do not match`() {
        assertFalse("Yoga Mayhem".fuzzyMatches("Jazz 404: New Moon Drifters"))
        assertFalse("Brian Brooks Sundown Rising".fuzzyMatches("Cole Phillips"))
    }

    @Test
    fun `a title with no words has no similarity to one with words`() {
        assertEquals(0.0, "[ ]".similarity("Durza"))
    }
}
