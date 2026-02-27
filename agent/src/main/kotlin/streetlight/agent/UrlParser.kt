package streetlight.agent

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.params.LLMParams
import kabinet.console.globalConsole
import kampfire.utils.takeEllipsis
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import java.io.File

class UrlParser(apiKey: String) {
    val executor = simpleGoogleAIExecutor(apiKey)
    val console = globalConsole.getHandle(UrlParser::class)
    val cache = mutableMapOf<String, String>()

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

    suspend inline fun <reified T: Any> readHtml(url: String, instructions: String): T? {
        val cached = cache[url]
        if (cached != null) return tryDecode(cached)

        console.log("reading url: $url")
        val content = readContent(url)
        console.log("content length: ${content.length}")
        // console.log(T::class.toBasicSchema())
        val filename = toFilenameFormat(url)
        val file = File("../debug/$filename.html")
        file.parentFile.mkdirs()
        file.writeText(content)
        // val body = readBody(content)

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

        val json = executor.execute(prompt, GoogleModels.Gemini2_5Flash).first().content
        cache[url] = json

        if (cache.size > 10) {
            val firstKey = cache.keys.firstOrNull()
            firstKey?.let { cache.remove(it) }
        }

        return tryDecode(json)
    }

    suspend inline fun <reified T: Any> readImage(url: String, instructions: String): T? {

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
        cache[url] = json

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
