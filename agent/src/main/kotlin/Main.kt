import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import klutch.environment.readEnvFromPath
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val env = readEnvFromPath()
    // Get an API key from the OPENAI_API_KEY environment variable
    val apiKey = env.read("GEMINI_KEY_RATE_LIMIT_A")

    val content = readContent("https://www.reddit.com/r/AuroraCO/comments/1r33pww/dog_runaway_from_petco_mississippi_and_havana_area/")
    val body = readBody(content)

    // Create an agent
    val agent = AIAgent(
        promptExecutor = simpleGoogleAIExecutor(apiKey),
        systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
        llmModel = GoogleModels.Gemini2_5Flash,
        temperature = 0.7,
        toolRegistry = ToolRegistry {
            tool(SayToUser)
        },
        maxIterations = 30
    )

    val result = agent.run("Provide your best estimate of the gps coordinates of the location described in this user post: $body")
    println(result)
}