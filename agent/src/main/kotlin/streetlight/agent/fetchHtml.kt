package streetlight.agent

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpStatusCode

private val log = KotlinLogging.logger("fetchHtml")

suspend fun fetchHtml(url: String): String? {
    log.info { "fetching url: ${url.take(50)}" }
    val response: HttpResponse = httpClient.get(url)
    if (response.status != HttpStatusCode.OK) return null.also {
        log.info { "Invalid status code: ${response.status}" }
    }
    return response.bodyAsText()
}

fun parseDocument(html: String, baseUri: String): Document? {
    if (!html.looksLikeHtml()) return null
    return Ksoup.parse(html, baseUri)
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