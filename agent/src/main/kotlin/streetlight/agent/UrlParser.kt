package streetlight.agent

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.params.LLMParams
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import kabinet.console.globalConsole
import kampfire.utils.takeEllipsis
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import looksLikeHtml
import java.io.File

class UrlParser(apiKey: String) {
    val executor = simpleGoogleAIExecutor(apiKey)
    val console = globalConsole.getHandle(UrlParser::class)
    val cache = mutableMapOf<Int, ParserContent>()
    val trimmer = HtmlTrimmer()

//    private val agent = AIAgent(
//        promptExecutor = executor,
//        systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
//        llmModel = GoogleModels.Gemini2_5Flash,
//        temperature = 0.7,
//        toolRegistry = ToolRegistry {
//            tool(SayToUser)
//        },
//        maxIterations = 30,
//    )

    suspend inline fun <reified T: Any> readUrl(url: String, instructions: String): ParserResult<T>? {
        val content = withCache(url.hashCode()) {
            console.log("reading url: $url")
            val content = readContent(url)
            console.log("caching content length: ${content.length}")
            val filename = toFilenameFormat(url)
            val file = File("../debug/$filename.html")
            file.parentFile.mkdirs()
            file.writeText(content)
            readHtmlContent<T>(url, content, instructions)
        } ?: return null

        return resultOf(content)
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

    inline fun <reified T> resultOf(content: ParserContent) = content.json.let { tryDecode<T>(it) }?.let {
        ParserResult(content.document, it)
    }

    suspend inline fun <reified T: Any> readHtml(url: String, html: String, instructions: String): ParserResult<T>? {
        val content = withCache(html.hashCode()) {
            console.log("html length: ${html.length}")
            readHtmlContent<T>(url, html, instructions)
        } ?: return null

        return resultOf(content)
    }

    suspend inline fun <reified T: Any> readHtmlContent(
        url: String,
        rawContent: String,
        instructions: String
    ): ParserContent? {
        if (!rawContent.looksLikeHtml()) {
            console.logError("not likely html:\n${rawContent.take(80)}")
            return null
        }
        val doc = Ksoup.parse(html = rawContent)
        val content = trimmer.trimHtml(doc)
        val percentReduced = (100.0 * (rawContent.length - content.length) / rawContent.length).toInt()
        val report = "from ${rawContent.length} to ${content.length} ($percentReduced%)"
        console.log(report)
        val filename = toFilenameFormat(url)
        val file = File("../debug/$filename.reduced.html")
        file.parentFile.mkdirs()
        file.writeText(content)

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

    suspend inline fun <reified T: Any> readImage(url: String, instructions: String): ParserResult<T>? {
        val cacheKey = url.hashCode()
        val cached = cache[cacheKey]
        if (cached != null) return ParserResult<T>(
            document = cached.document,
            value = tryDecode(cached.json)
        )

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

fun toFilenameFormat(input: String): String =
    input
        .take(64).lowercase()
        .replace(Regex("[^A-Za-z0-9]"), "_")

val jsonConfig = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

private val htmlStart = Regex("""^\s*(<!DOCTYPE\s+html|<html|<[a-zA-Z]+)""", RegexOption.IGNORE_CASE)

fun String.looksLikeHtml(): Boolean = htmlStart.containsMatchIn(this)

data class ParserContent(
    val document: Document?,
    val json: String,
)

data class ParserResult<T>(
    val document: Document?,
    val value: T?
)