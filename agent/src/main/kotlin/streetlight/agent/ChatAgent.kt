package streetlight.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.AIAgent.Companion.invoke
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.ToolRegistry.Companion.invoke
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.message.Message
import ai.koog.prompt.params.LLMParams
import kabinet.console.globalConsole
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val console = globalConsole.getHandle(ChatAgent::class)

class ChatAgent(
    apiKey: String,
) {
    private val socket = ChatSocket()

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
    var messages: List<Message> = emptyList()

    suspend fun connect() = coroutineScope {
        launch {
            socket.connect()
        }

        socket.messageFlow.collect { message ->
            console.log("received: $message")
            val prompt = prompt(
                id = "dev-assistant",
                params = LLMParams(
                    temperature = 0.5,
                )
            ) {
                // Add a system message to set the context
                if (messages.none { it.role == Message.Role.System}) {
                    system("You are a helpful assistant. Answer user questions concisely.")
                }

                messages(messages)

                user(message)
            }

            messages = prompt.messages

            val response = executor.execute(prompt, GoogleModels.Gemini2_5Flash).first()
            messages += response
            console.log("respending: ${response.content}")
            socket.send(response.content)
        }
    }
}