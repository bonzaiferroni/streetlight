package streetlight.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import kotlinx.datetime.Clock
import streetlight.model.data.ChatMessage

class ChatAgent(
    apiKey: String,
    private val model: LLModel = GoogleModels.Gemini2_5Flash,
    private val onResponse: suspend (ChatMessage) -> Unit
) {
    private val executor = simpleGoogleAIExecutor(apiKey)

    private val agent get() = AIAgent(
        promptExecutor = executor,
        systemPrompt = "Yer a pirate and helpful assistant, first mate. Keep yer answers warm and concise.",
        llmModel = model,
        temperature = 0.7,
        toolRegistry = ToolRegistry {
            tools(UrlToolSet())
        },
        maxIterations = 30,
    )

    var messages = mutableListOf<ChatMessage>()
    val identifier = "botbot"

    suspend fun takeInput(message: ChatMessage) {
        messages.add(message)
        promptRespond(message)
    }

    private suspend fun agentRespond(message: ChatMessage) {
        val response = agent.run(message.text.take(200))
        val reply = ChatMessage(identifier, response, Clock.System.now())
        messages.add(reply)
        onResponse(reply)
    }

    private suspend fun promptRespond(message: ChatMessage) {
        val prompt = prompt(
            id = "dev-assistant",
            params = LLMParams(
                temperature = 0.5,
            )
        ) {
            system("Yer a pirate and helpful assistant, first mate. Keep yer answers warm and concise.")

            messages.takeLast(20).forEach { message ->
                if (message.source == identifier) {
                    assistant(message.text)
                } else {
                    user(message.text)
                }
            }
        }

        val response = executor.execute(prompt, model).first()
        val reply = ChatMessage(identifier, response.content, Clock.System.now())
        messages.add(reply)
        // console.log("responding: ${response.content}")
        onResponse(reply)
    }
}