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
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpStatusCode
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toProblem
import kampfire.model.toUrl

private val log = KotlinLogging.logger("fetchHtml")

suspend fun fetchHtml(url: Url): Outcome<String> {
    log.info { "fetching url: ${url.value.take(50)}" }
    val response: HttpResponse = httpClient.get(url.value)
    if (response.status != HttpStatusCode.OK) return response.status.toProblem()
    return Ok(response.bodyAsText())
}

@Deprecated("Use overload that takes Url")
suspend fun fetchHtml(url: String) = fetchHtml(url.toUrl())

fun parseDocument(html: String, url: Url): Document? {
    if (!html.looksLikeHtml()) return null
    return Ksoup.parse(html, url.value)
}

private val htmlStart = Regex("""^\s*(<!DOCTYPE\s+html|<html|<[a-zA-Z]+)""", RegexOption.IGNORE_CASE)

fun String.looksLikeHtml(): Boolean = htmlStart.containsMatchIn(this)

private val httpClient by lazy {
    HttpClient {
        defaultRequest {
//            header("User-Agent", "Streetlight/1.0")
            header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:143.0) Gecko/20100101 Firefox/143.0")
            header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            header("Accept-Language", "en-US,en;q=0.5")
            header("Connection", "keep-alive")
        }
    }
}



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