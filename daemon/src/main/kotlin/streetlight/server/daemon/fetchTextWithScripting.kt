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
import kampfire.model.toUrl
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import streetlight.agent.FetchText
import streetlight.agent.StreetlightAgent
import streetlight.agent.fetchText
import streetlight.model.data.FetchMode
import java.net.URI
import kotlin.time.Clock
import kotlin.time.TimeSource

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
suspend fun fetchTextWithScripting(initialUrl: Url): Outcome<FetchText> = withContext(browserContext) {
    logger.info { "fetching url with scripting: ${initialUrl.value.take(100)}" }
    val start = TimeSource.Monotonic.markNow()
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
                    initialUrl, page.url().toUrl(), page.content(), Clock.System.now(),
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

/**
 * Waits until the body's visible text has held its length for [settleMillis], so content rendered after the
 * load event is included, giving up [maxSettleMillis] after load.
 */
private fun Page.waitForSettledText() {
    val start = System.currentTimeMillis()
    var length = bodyTextLength()
    var stableSince = start
    while (System.currentTimeMillis() - start < maxSettleMillis) {
        waitForTimeout(pollMillis.toDouble())
        val next = bodyTextLength()
        val now = System.currentTimeMillis()
        if (next != length) {
            length = next
            stableSince = now
        } else if (now - stableSince >= settleMillis) {
            return
        }
    }
}

private fun Page.bodyTextLength() = runCatching { innerText("body").length }.getOrDefault(0)

/**
 * Replaces each child iframe holding at least [minFrameTextChars] of text with a div of its body html, since
 * [Page.content] leaves out iframe contents. The div carries the frame's url as `data-frame-src`, and relative
 * links inside it are made absolute against that url.
 */
private fun Page.foldFrames() {
    frames().filter { it != mainFrame() && it.parentFrame() == mainFrame() }.forEach { frame ->
        runCatching {
            if (frame.innerText("body").length < minFrameTextChars) return@forEach
            val html = frame.evaluate(absoluteBodyHtmlScript) as String
            frame.frameElement().evaluate(foldFrameScript, mapOf("html" to html, "src" to frame.url()))
        }.onFailure { logger.warn { "unable to fold frame ${frame.url().take(100)}: ${it.message}" } }
    }
}

private const val settleMillis = 1_000L
private const val pollMillis = 250L
private const val maxSettleMillis = 8_000L
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