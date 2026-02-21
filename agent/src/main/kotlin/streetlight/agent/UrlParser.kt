package streetlight.agent

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.params.LLMParams
import kabinet.console.globalConsole
import kotlinx.serialization.json.Json
import java.io.File


class UrlParser(apiKey: String) {
    val executor = simpleGoogleAIExecutor(apiKey)
    val console = globalConsole.getHandle(UrlParser::class)

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

    suspend inline fun <reified T: Any> read(url: String, instructions: String): T {
        val content = readContent(url)
        val schema = T::class.toBasicSchema()
        console.log(schema)
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

            user("$instructions\n\nHere is the HTML: $content")
        }

        val json = executor.execute(prompt, GoogleModels.Gemini2_5Flash).first().content

        return Json.decodeFromString(json)
    }
}

fun toFilenameFormat(input: String): String =
    input
        .take(64).lowercase()
        .replace(Regex("[^A-Za-z0-9]"), "_")