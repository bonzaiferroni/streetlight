package streetlight.agent

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.params.LLMParams
import com.fleeksoft.ksoup.nodes.Document
import kabinet.console.globalConsole
import kampfire.utils.takeEllipsis
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import looksLikeHtml
import java.io.File

// td: refactor, this is a hot mess
class UrlParser(apiKey: String) {
    val executor = simpleGoogleAIExecutor(apiKey)
    val console = globalConsole.getHandle(UrlParser::class)
    val cache = mutableMapOf<Int, ParserContent>()
    val trimmer = HtmlTrimmer()

    suspend inline fun <reified T: Any> readHtml(url: String, doc: Document, instructions: String): T? {
        val content = withCache(doc.hashCode()) {
            readHtmlContent<T>(url, doc, instructions)
        } ?: return null

        return tryDecode(content.json)
    }

    inline fun withCache(cacheKey: Int, block: () -> ParserContent?): ParserContent? = cache[cacheKey] ?: block().also { content ->
        if (content != null) {
            cache[cacheKey] = content

            if (cache.size > 25) {
                val firstKey = cache.keys.firstOrNull()
                firstKey?.let { key -> cache.remove(key) }
            }
        }
    }

    suspend inline fun <reified T: Any> readHtmlContent(
        url: String,
        doc: Document,
        instructions: String
    ): ParserContent? {
        val content = trimmer.trimHtml(doc)

        val prompt = prompt(
            id = "dev-assistant",
            params = LLMParams(
                temperature = 0.5,
                schema = T::class.toBasicSchema()
            )
        ) {
            system("You read web pages and extract relevant information as json.")

            user("$instructions\n\nFor reference, here is the url:\n$url\n\nHere is the HTML:\n$content")
        }

        val json = try {
            executor.execute(prompt, GoogleModels.Gemini2_5Flash).first().content
        } catch (e: LLMClientException) {
            console.log(e)
            null
        } ?: return null

        return ParserContent(
            document = doc,
            json = json
        )
    }

    suspend inline fun <reified T: Any> readImage(url: String, instructions: String): T? {
        val cacheKey = url.hashCode()
        val cached = cache[cacheKey]
        if (cached != null) return tryDecode(cached.json)

        val prompt = prompt(
            id = "dev-assistant",
            params = LLMParams(
                temperature = 0.5,
                schema = T::class.toBasicSchema()
            )
        ) {
            system("You read images and extract relevant information as json.")

            user {
                +instructions

//                image(
//                    ContentPart.Image(
//                        content = AttachmentContent.URL(url),
//                        format = "jpg",
//                        mimeType = "image/jpg",
//                        fileName = "c285c6c0-0b15-4113-a1e9-0add48984ac9.jpg"
//                    )
//                )
                image(Path(url))
            }
        }

        val json = executor.execute(prompt, GoogleModels.Gemini2_5Flash).first().content
        cache[cacheKey] = ParserContent(
            document = null,
            json = json
        )

        if (cache.size > 10) {
            val firstKey = cache.keys.firstOrNull()
            firstKey?.let { cache.remove(it) }
        }
        return tryDecode(json)
    }

    inline fun <reified T> tryDecode(text: String): T? = try {
        decodeLenient(text)
    } catch (e: Exception) {
        console.logThrowable(e)
        console.logError("unable to decode structured llm response:\n${text.takeEllipsis(400)}")
        null
    }
}

data class ParserContent(
    val document: Document?,
    val json: String,
)