package streetlight.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.AIAgent.Companion.invoke
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.ToolRegistry.Companion.invoke
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.params.LLMParams
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import streetlight.model.data.StoryInfo

class UrlReader(apiKey: String) {
    private val agent = AIAgent(
        promptExecutor = simpleGoogleAIExecutor(apiKey),
        systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
        llmModel = GoogleModels.Gemini2_5Flash,
        temperature = 0.7,
        toolRegistry = ToolRegistry {
            tool(SayToUser)
        },
        maxIterations = 30
    )

    val executor = simpleGoogleAIExecutor(apiKey)

    suspend inline fun <reified T: Any> read(url: String, message: String): T {
        val content = readContent(url)
        val body = readBody(content)

        val prompt = prompt(
            id = "dev-assistant",
            params = LLMParams(
                temperature = 0.5,
                schema = T::class.toBasicSchema()
            )
        ) {
            // Add a system message to set the context
            system("You read web pages and extract relevant information as json.")

            // Add a user message
            user("$message\n\nHere is the HTML: $body")
        }

        val json = executor.execute(prompt, GoogleModels.Gemini2_5Flash).first().content

        return Json.decodeFromString(json)
        // return agent.run("Provide your best estimate of the gps coordinates of the location described in this user post: $body")
    }
}
