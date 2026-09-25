package streetlight.agent

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Outcome
import kampfire.model.Url
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface HtmlParserClient {
    suspend fun <T: Any> readHtml(url: Url, doc: Document, instructions: String, type: KClass<T>): Outcome<T>
    suspend fun <T: Any> readHtml(
        url: Url,
        doc: Document,
        instructions: String,
        type: KType,
        retryCount: Int = 3,
    ): Outcome<T>
    suspend fun <T> readImage(url: String, instructions: String, type: KType): T?
}

suspend inline fun <reified T: Any> HtmlParserClient.readHtml(
    url: Url,
    doc: Document,
    instructions: String,
    retryCount: Int = 3,
): Outcome<T> = readHtml(url, doc, instructions, typeOf<T>(), retryCount)
