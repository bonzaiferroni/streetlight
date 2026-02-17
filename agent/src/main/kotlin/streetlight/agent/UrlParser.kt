package streetlight.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.params.LLMParams
import kotlinx.serialization.json.Json

class UrlParser(apiKey: String) {
    val executor = simpleGoogleAIExecutor(apiKey)

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

    suspend inline fun <reified T: Any> read(url: String): T {
        val content = readContent(url)
        val body = readBody(content)

        val prompt = prompt(
            id = "dev-assistant",
            params = LLMParams(
                temperature = 0.5,
                schema = T::class.toBasicSchema()
            )
        ) {
            system("You read web pages and extract relevant information as json.")

            user("$instructions\n\nHere is the HTML: $body")
        }

        val json = executor.execute(prompt, GoogleModels.Gemini2_5Flash).first().content

        return Json.decodeFromString(json)
    }

    companion object {
        val instructions = "Read the following html. It is a user post or a news article. " +
                "Your job is to extract the requested information as json. " +
                "Provide a headline that describes the story, if one is not present in the content then generate an appropriate headline. " +
                "Provide a brief description of the story, one or two sentences. " +
                "Provide the url of any feature image that can be identified with meta information or given its placement in the body. " +
                "If the content refers to a location, provide an estimate of the location (longitude/latitude). " +
                "Provide the time that the story was posted. "
    }
}
