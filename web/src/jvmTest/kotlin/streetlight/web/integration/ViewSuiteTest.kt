package streetlight.web.integration

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ViewSuiteTest {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var runner: ViewPageRunner

    @BeforeAll
    fun startBrowser() {
        val isHeaded = System.getenv("VIEW_HEADED")?.toBoolean() ?: false
        playwright = Playwright.create()
        browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(!isHeaded))
        runner = ViewPageRunner(browser, Path.of(System.getProperty("view.dist")))
    }

    @AfterAll
    fun stopBrowser() {
        runner.close()
        browser.close()
        playwright.close()
    }

    @TestFactory
    fun `each view test runs in a fresh page`(): List<DynamicTest> {
        val names = runner.discover()
        assertTrue(names.isNotEmpty(), "no view tests were discovered")

        return names.map { name ->
            require(name.none { it in INCLUDE_SYNTAX }) { "test name uses include syntax and cannot be selected: $name" }
            DynamicTest.dynamicTest(name) {
                val run = runner.run(name)
                run.output.forEach(::println)
                assertEquals(1, run.started.size, "expected exactly one test to run for: $name")
                assertTrue(run.failures.isEmpty(), run.failures.joinToString("\n"))
            }
        }
    }
}

private const val INCLUDE_SYNTAX = ",*!"
