import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import klutch.environment.readEnvFromPath
import kotlinx.coroutines.runBlocking
import streetlight.agent.ChatAgent
import streetlight.agent.UrlReader
import streetlight.agent.readBody
import streetlight.agent.readContent

fun main(): Unit = runBlocking {
    val env = readEnvFromPath()
    // Get an API key from the OPENAI_API_KEY environment variable
    val apiKey = env.read("GEMINI_KEY_RATE_LIMIT_A")
    val agent = ChatAgent(apiKey)
    agent.connect()

//    val result = agent.read("https://www.reddit.com/r/AuroraCO/comments/1r33pww/dog_runaway_from_petco_mississippi_and_havana_area/")
//    println(result)
}