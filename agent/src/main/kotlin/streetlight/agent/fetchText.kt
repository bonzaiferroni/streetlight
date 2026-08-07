package streetlight.agent

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.select.Elements
import com.fleeksoft.ksoup.select.Selector
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toProblem
import kampfire.model.toUrl
import kotlin.time.Clock
import kotlin.time.Instant

private val logger = KotlinLogging.logger("ktor-fetch-client")

private val httpClient by lazy {
    HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = StreetlightAgent.Timeout.toLong()
            connectTimeoutMillis = StreetlightAgent.Timeout.toLong()
            socketTimeoutMillis = StreetlightAgent.Timeout.toLong()
        }
        defaultRequest {
            header("User-Agent", StreetlightAgent.UserAgent)
            header("Accept", StreetlightAgent.Accept)
            header("Accept-Language", StreetlightAgent.AcceptLanguage)
            header("Connection", StreetlightAgent.Connection)
        }
    }
}

suspend fun fetchText(initialUrl: Url): Outcome<FetchText> {
    logger.info { "fetching url: ${initialUrl.value.take(100)}" }
    val response: HttpResponse = httpClient.get(initialUrl.value)
    if (response.status != HttpStatusCode.OK) {
        logger.info { "non-OK ${response.status} for ${initialUrl.value}, location=${response.headers[HttpHeaders.Location]}" }
        return response.status.toProblem()
    }
    return Ok(FetchText(
        fetchUrl = initialUrl,
        pageUrl = response.request.url.toString().toUrl(),
        text = response.bodyAsText(),
        fetchedAt = Clock.System.now()
    ))
}

suspend fun fetchText(url: String) = fetchText(url.toUrl())

fun parseHtmlDocument(html: String, url: Url): Outcome<Document> {
    if (!html.looksLikeHtml()) return Problem("Document was not html")
    return Ok(Ksoup.parse(html, url.value))
}

private val htmlStart = Regex("""^\s*(<!DOCTYPE\s+html|<html|<[a-zA-Z]+)""", RegexOption.IGNORE_CASE)

fun String.looksLikeHtml(): Boolean = htmlStart.containsMatchIn(this)

fun Element.tryQuery(selector: String): Outcome<Elements> {
    if (selector == ".") return Ok(Elements(this))

    return try {
        Ok(select(selector))
    } catch (e: Selector.SelectorParseException) {
        Problem("Malformed selector: $selector")
    } catch (e: IllegalArgumentException) {
        Problem("Invalid selector: $selector")
    }
}

data class FetchText(
    val fetchUrl: Url,
    val pageUrl: Url,
    val text: String,
    val fetchedAt: Instant,
)