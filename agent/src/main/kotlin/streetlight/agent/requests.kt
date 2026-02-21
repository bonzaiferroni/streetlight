package streetlight.agent

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.plugins.defaultRequest

suspend fun readContent(url: String): String {
    val response: HttpResponse = httpClient.get(url)
    return response.bodyAsText()
}

fun readBody(html: String): String {
    val document = Ksoup.parse(html)
    return document.body().html()
}


private val httpClient by lazy {
    HttpClient {
        defaultRequest {
            header("User-Agent", "Streetlight/1.0")
//            header(
//                "User-Agent",
//                "Mozilla/5.0 (X11; Linux x86_64; rv:143.0) Gecko/20100101 Firefox/143.0"
//            )
            header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            header("Accept-Language", "en-US,en;q=0.5")
            header("Connection", "keep-alive")
        }
    }
}