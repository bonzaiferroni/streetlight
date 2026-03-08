package streetlight.agent

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.params.LLMParams
import kabinet.console.globalConsole
import kampfire.utils.takeEllipsis
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import looksLikeHtml
import java.io.File

class UrlParser(apiKey: String) {
    val executor = simpleGoogleAIExecutor(apiKey)
    val console = globalConsole.getHandle(UrlParser::class)
    val cache = mutableMapOf<Int, String>()
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

    suspend inline fun <reified T: Any> readUrl(url: String, instructions: String): T? {
        val json = withCache(url.hashCode()) {
            console.log("reading url: $url")
            val content = readContent(url)
            console.log("caching content length: ${content.length}")
            val filename = toFilenameFormat(url)
            val file = File("../debug/$filename.html")
            file.parentFile.mkdirs()
            file.writeText(content)
            readHtmlContent<T>(url, content, instructions)
        } ?: return null

        return tryDecode(json)
    }

    inline fun withCache(cacheKey: Int, block: () -> String?): String? = cache[cacheKey] ?: block().also { json ->
        if (json != null) {
            cache[cacheKey] = json

            if (cache.size > 25) {
                val firstKey = cache.keys.firstOrNull()
                firstKey?.let { key -> cache.remove(key) }
            }
        }
    }

    suspend inline fun <reified T: Any> readHtml(url: String, html: String, instructions: String): T? {
        val json = withCache(html.hashCode()) {
            console.log("html length: ${html.length}")
            readHtmlContent<T>(url, html, instructions)
        } ?: return null

        return tryDecode(json)
    }

    suspend inline fun <reified T: Any> readHtmlContent(
        url: String,
        rawContent: String,
        instructions: String
    ): String? {
        if (!rawContent.looksLikeHtml()) {
            console.logError("not likely html:\n${rawContent.take(80)}")
            return null
        }
        val content = trimmer.trimHtml(rawContent)
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

        return executor.execute(prompt, GoogleModels.Gemini2_5Flash).first().content
    }

    suspend inline fun <reified T: Any> readImage(url: String, instructions: String): T? {
        val cacheKey = url.hashCode()
        val cached = cache[cacheKey]
        if (cached != null) return tryDecode(cached)

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
        cache[cacheKey] = json

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

fun String.looksLikeHtml(): Boolean =
    htmlStart.containsMatchIn(this)