package streetlight.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.message.Message
import ai.koog.prompt.params.LLMParams
import kabinet.console.globalConsole
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import streetlight.model.data.ChatMessage

private val console = globalConsole.getHandle(ChatAgent::class)

class ChatAgent(
    apiKey: String,
) {
    private val socket = HostChatSocket()

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
    var messages = mutableListOf<ChatMessage>()
    val identifier = "botbot"

    suspend fun connect() = coroutineScope {
        launch {
            socket.connect()
        }

        val connectedAt = Clock.System.now()

        socket.messageFlow.collect { message ->
            if (message.source == identifier) return@collect
            // console.log("received: $message")
            messages.add(message)
            if (message.sentAt > connectedAt)
                respond()
        }
    }

    private suspend fun respond() {
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

        val response = executor.execute(prompt, GoogleModels.Gemini2_5Flash).first()
        val message = ChatMessage(identifier, response.content, Clock.System.now())
        messages.add(message)
        // console.log("responding: ${response.content}")
        socket.send(message)
    }
}