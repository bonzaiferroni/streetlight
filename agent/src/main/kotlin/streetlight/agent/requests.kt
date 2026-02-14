package streetlight.agent

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import com.fleeksoft.ksoup.Ksoup

suspend fun readContent(url: String): String {
    val client = HttpClient()
    return try {
        val response: HttpResponse = client.get(url)
        response.bodyAsText()
    } finally {
        client.close()
    }
}

fun readBody(html: String): String {
    val document = Ksoup.parse(html)
    return document.body().html()
}
