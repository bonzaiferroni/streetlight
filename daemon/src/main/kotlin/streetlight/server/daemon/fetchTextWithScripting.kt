package streetlight.server.daemon

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.TimeoutError
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.WaitUntilState
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toProblem
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import streetlight.agent.StreetlightAgent
import streetlight.agent.fetchText
import streetlight.model.data.FetchMode
import java.net.URI

private val logger = KotlinLogging.logger("playwright-fetch-client")

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
private val browserContext = newSingleThreadContext("playwright-browser")

private val playwright by lazy { Playwright.create() }

private val browser by lazy {
    playwright.chromium().launch(
        BrowserType.LaunchOptions().setHeadless(true)
    )
}

private fun contextOptions() = Browser.NewContextOptions()
    .setUserAgent(StreetlightAgent.UserAgent)
    .setLocale(StreetlightAgent.Locale)
    .setExtraHTTPHeaders(
        mapOf("Accept-Language" to StreetlightAgent.AcceptLanguage)
    )

suspend fun fetchText(url: Url, mode: FetchMode) = when (mode) {
    FetchMode.Basic -> fetchText(url)
    FetchMode.Scripting -> fetchTextWithScripting(url)
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun fetchTextWithScripting(url: Url): Outcome<String> = withContext(browserContext) {
    logger.info { "fetching url with scripting: ${url.value.take(100)}" }
    browser.newContext(contextOptions()).use { context ->
        context.setDefaultTimeout(StreetlightAgent.Timeout.toDouble())
        context.setDefaultNavigationTimeout(StreetlightAgent.Timeout.toDouble())
        context.route("**") { route ->
            val request = route.request()
            val host = runCatching { URI(request.url()).host.orEmpty() }.getOrDefault("")
            when {
                request.resourceType() in blockedTypes -> route.abort()
                // host in blockedHosts -> route.abort()
                else -> route.resume()
            }
        }
        context.newPage().use { page ->
            // page.onRequest { logger.info { "${it.resourceType()}: ${it.url().take(120)}" } }
            val response = page.navigate(
                url.value,
                Page.NavigateOptions()
                    .setTimeout(StreetlightAgent.Timeout.toDouble())
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
            ) ?: return@withContext Problem("Unable to navigate: $url")

            val status = HttpStatusCode.fromValue(response.status())
            if (status != HttpStatusCode.OK) return@withContext status.toProblem()

            return@withContext try {
                page.waitForLoadState(LoadState.LOAD)
                // val hasPrice = page.evaluate("document.body.innerText.includes('\$')")
                // logger.info { "price text present in live page: $hasPrice" }
                // logger.info { "price probe: ${page.evaluate(priceProbeScript)}" }
                Ok(page.content())
            } catch (e: TimeoutError) {
                logger.warn { "timeout fetching ${url.value.take(100)}" }
                Problem("Url timed out: $url")
            } catch (e: PlaywrightException) {
                logger.warn(e) { "playwright failure fetching ${url.value.take(100)}" }
                Problem("Playwright exception: ${e.message}")
            }
        }
    }
}

fun closeBrowser() {
    browser.close()
    playwright.close()
}

private val blockedHosts = setOf(
    "www.googletagmanager.com", "www.google-analytics.com", "analytics.google.com",
    "stats.g.doubleclick.net", "googleads.g.doubleclick.net", "ad.doubleclick.net",
    "www.googleadservices.com", "connect.facebook.net", "www.facebook.com",
    "www.redditstatic.com", "alb.reddit.com", "arttrk.com",
    "js.stripe.com", "m.stripe.network", "m.stripe.com",
    "www.google.com/recaptcha", "www.gstatic.com"
)

private val blockedTypes = setOf("image", "media", "font", "stylesheet")

private val priceProbeScript = """
    () => {
        const dollar = String.fromCharCode(36);
        const found = [];
        const visit = (root, inShadow) => {
            for (const el of root.querySelectorAll('*')) {
                if (el.shadowRoot) visit(el.shadowRoot, true);
                const own = Array.from(el.childNodes)
                    .filter(n => n.nodeType === 3)
                    .map(n => n.textContent.trim())
                    .join(' ')
                    .trim();
                if (own.includes(dollar)) {
                    found.push({
                        tag: el.tagName.toLowerCase(),
                        classes: String(el.className || ''),
                        inShadow: inShadow,
                        text: own.slice(0, 120),
                        html: el.outerHTML.slice(0, 400)
                    });
                }
            }
        };
        visit(document, false);
        return JSON.stringify(found.slice(0, 20), null, 2);
    }
""".trimIndent()