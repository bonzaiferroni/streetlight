package streetlight.server.daemon.agent

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Outcome
import kampfire.model.Url
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/** Reads a page's html with a language model, into the shape its instructions ask for. */
interface HtmlParserClient {
    suspend fun <T: Any> readHtml(
        url: Url,
        doc: Document,
        instructions: String,
        type: KType,
        retryCount: Int = 3,
        observer: HtmlParseObserver? = null,
    ): Outcome<T>
}

suspend inline fun <reified T: Any> HtmlParserClient.readHtml(
    url: Url,
    doc: Document,
    instructions: String,
    retryCount: Int = 3,
    observer: HtmlParseObserver? = null,
): Outcome<T> = readHtml(url, doc, instructions, typeOf<T>(), retryCount, observer)
