package streetlight.server.daemon.crawler

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.TimeoutError
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.WaitUntilState
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy
import org.apache.hc.core5.util.TimeValue
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.normalize
import kampfire.model.toDataOrNull
import kampfire.model.toProblem
import kampfire.model.toUrl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import streetlight.model.data.FetchMode
import streetlight.model.data.Origin
import streetlight.model.data.OriginId
import streetlight.server.daemon.agent.FetchText
import streetlight.server.model.DaoFacade
import streetlight.server.model.StreetlightAgent
import kotlin.time.Clock
import kotlin.time.TimeSource

private val logger = KotlinLogging.logger("page-fetcher")

/**
 * The fetcher of the crawler's pages: each one waits on its origin's robots gate, then is read with a plain request
 * or in a scripted browser.
 */
class PageFetcher(private val dao: DaoFacade) {

    private val gateMutex = Mutex()
    private val robotGates = mutableMapOf<OriginId, RobotGate>()

    private val httpClient by lazy {
        HttpClient(Apache5) {
            engine {
                customizeClient {
                    setRetryStrategy(DefaultHttpRequestRetryStrategy(1, TimeValue.ofSeconds(30)))
                }
            }
            install(HttpRedirect) {
                allowHttpsDowngrade = true
            }
            install(HttpTimeout) {
                requestTimeoutMillis = StreetlightAgent.Timeout.toLong()
                connectTimeoutMillis = StreetlightAgent.Timeout.toLong()
                socketTimeoutMillis = StreetlightAgent.Timeout.toLong()
            }
            defaultRequest {
                header("User-Agent", StreetlightAgent.UserAgent)
                header("Accept", StreetlightAgent.Accept)
                header("Accept-Language", StreetlightAgent.AcceptLanguage)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val browserContext = newSingleThreadContext("playwright-browser")
    private val playwright = lazy { Playwright.create() }
    private val browser = lazy {
        playwright.value.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
    }

    /** The page at [url] on [origin], fetched in [mode] once its robots gate opens. */
    suspend fun fetch(url: Url, origin: Origin, mode: FetchMode): Outcome<FetchText> {
        if (!readRobotGate(origin).waitUntilOpen(url)) return RobotProblem.Disallowed
        return when (mode) {
            FetchMode.Basic -> fetchBasic(url)
            FetchMode.Scripting -> fetchWithScripting(url)
        }
    }

    /** Closes the browser, when one was opened. */
    fun close() {
        if (browser.isInitialized()) browser.value.close()
        if (playwright.isInitialized()) playwright.value.close()
    }

    /** The robots gate of [origin], read from its stored robots.txt or fetched and stored once. */
    private suspend fun readRobotGate(origin: Origin): RobotGate = gateMutex.withLock {
        robotGates[origin.originId]?.let { return@withLock it }
        val txt = origin.robotsTxt ?: fetchBasic(origin.originId.toRobotsTxtUrl()).toDataOrNull()?.text?.also {
            dao.origin.updateRobotsTxt(origin.originId, it)
        }
        txt.toRobotGate().also { robotGates[origin.originId] = it }
    }

    /** The page at [initialUrl], read with a plain request. */
    private suspend fun fetchBasic(initialUrl: Url): Outcome<FetchText> {
        logger.info { "fetching url: ${initialUrl.value.take(100)}" }
        val start = TimeSource.Monotonic.markNow()
        return try {
            val response: HttpResponse = httpClient.get(initialUrl.value)
            if (response.status != HttpStatusCode.OK) {
                logger.info { "non-OK ${response.status} for ${initialUrl.value}, location=${response.headers[HttpHeaders.Location]}" }
                return response.status.toProblem()
            }
            Ok(FetchText(
                servedUrl = response.request.url.toString().toUrl().normalize(),
                text = response.bodyAsText(),
                fetchedAt = Clock.System.now(),
                millis = start.elapsedNow().inWholeMilliseconds,
            ))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.info { "fetch failed for ${initialUrl.value}: $e" }
            Problem("Fetch failed: ${e.message ?: e::class.simpleName}")
        }
    }

    /** The page at [initialUrl], read in the browser once its visible text has settled, its frames folded in. */
    private suspend fun fetchWithScripting(initialUrl: Url): Outcome<FetchText> = withContext(browserContext) {
        logger.info { "fetching url with scripting: ${initialUrl.value.take(100)}" }
        val start = TimeSource.Monotonic.markNow()
        browser.value.newContext(contextOptions()).use { context ->
            context.setDefaultTimeout(StreetlightAgent.Timeout.toDouble())
            context.setDefaultNavigationTimeout(StreetlightAgent.Timeout.toDouble())
            context.route("**") { route ->
                when (route.request().resourceType()) {
                    in blockedTypes -> route.abort()
                    else -> route.resume()
                }
            }
            context.newPage().use { page ->
                val response = page.navigate(
                    initialUrl.value,
                    Page.NavigateOptions()
                        .setTimeout(StreetlightAgent.Timeout.toDouble())
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                ) ?: return@withContext Problem("Unable to navigate: $initialUrl")

                val status = HttpStatusCode.fromValue(response.status())
                if (status != HttpStatusCode.OK) return@withContext status.toProblem()

                return@withContext try {
                    page.waitForLoadState(LoadState.LOAD)
                    page.waitForSettledText()
                    page.foldFrames()
                    Ok(FetchText(
                        page.url().toUrl().normalize(), page.content(), Clock.System.now(),
                        millis = start.elapsedNow().inWholeMilliseconds,
                    ))
                } catch (e: TimeoutError) {
                    logger.warn { "timeout fetching ${initialUrl.value.take(100)}" }
                    Problem("Url timed out: $initialUrl")
                } catch (e: PlaywrightException) {
                    logger.warn(e) { "playwright failure fetching ${initialUrl.value.take(100)}" }
                    Problem("Playwright exception: ${e.message}")
                }
            }
        }
    }
}

private fun contextOptions() = Browser.NewContextOptions()
    .setUserAgent(StreetlightAgent.UserAgent)
    .setLocale(StreetlightAgent.Locale)
    .setExtraHTTPHeaders(
        mapOf("Accept-Language" to StreetlightAgent.AcceptLanguage)
    )

/** Scrolls down with the mouse wheel until the page's visible text, frames included, has held its length for [settleMillis], at most [maxSettleMillis]. */
private fun Page.waitForSettledText() {
    val start = System.currentTimeMillis()
    var length = visibleTextLength()
    var stableSince = start
    while (System.currentTimeMillis() - start < maxSettleMillis) {
        runCatching { mouse().wheel(0.0, scrollPixels) }
        waitForTimeout(pollMillis.toDouble())
        val next = visibleTextLength()
        val now = System.currentTimeMillis()
        if (next != length) {
            length = next
            stableSince = now
        } else if (now - stableSince >= settleMillis) {
            return
        }
    }
}

private fun Page.visibleTextLength() = frames().sumOf { frame -> runCatching { frame.innerText("body").length }.getOrDefault(0) }

/** Replaces each child iframe holding at least [minFrameTextChars] of text with a div of its body html, marked with the frame's url. */
private fun Page.foldFrames() {
    frames().filter { it != mainFrame() && it.parentFrame() == mainFrame() }.forEach { frame ->
        runCatching {
            if (frame.innerText("body").length < minFrameTextChars) return@forEach
            val html = frame.evaluate(absoluteBodyHtmlScript) as String
            frame.frameElement().evaluate(foldFrameScript, mapOf("html" to html, "src" to frame.url()))
        }.onFailure { logger.warn { "unable to fold frame ${frame.url().take(100)}: ${it.message}" } }
    }
}

private const val settleMillis = 3_000L
private const val pollMillis = 250L
private const val maxSettleMillis = 15_000L
private const val scrollPixels = 2_500.0
private const val minFrameTextChars = 200

private val absoluteBodyHtmlScript = """
    () => {
        const body = document.body.cloneNode(true);
        for (const el of body.querySelectorAll('[href], [src]')) {
            for (const attr of ['href', 'src']) {
                const value = el.getAttribute(attr);
                if (value) {
                    try { el.setAttribute(attr, new URL(value, document.baseURI).href); } catch (e) {}
                }
            }
        }
        return body.innerHTML;
    }
"""

private val foldFrameScript = """
    (frame, arg) => {
        const div = document.createElement('div');
        div.setAttribute('data-frame-src', arg.src);
        div.innerHTML = arg.html;
        frame.replaceWith(div);
    }
"""

private val blockedHosts = setOf(
    "www.googletagmanager.com", "www.google-analytics.com", "analytics.google.com",
    "stats.g.doubleclick.net", "googleads.g.doubleclick.net", "ad.doubleclick.net",
    "www.googleadservices.com", "connect.facebook.net", "www.facebook.com",
    "www.redditstatic.com", "alb.reddit.com", "arttrk.com",
    "js.stripe.com", "m.stripe.network", "m.stripe.com",
    "www.google.com/recaptcha", "www.gstatic.com"
)

private val blockedTypes = setOf("image", "media", "font")

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