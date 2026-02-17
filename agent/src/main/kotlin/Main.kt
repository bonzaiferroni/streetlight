import klutch.environment.readEnvFromPath
import kotlinx.coroutines.runBlocking
import streetlight.agent.ChatAgentConnection
import streetlight.server.plugins.connectDb

fun main(): Unit = runBlocking {
    val env = readEnvFromPath()
    connectDb(env)
    // Get an API key from the OPENAI_API_KEY environment variable
    val apiKey = env.read("GEMINI_KEY_A")
    val agent = ChatAgentConnection(apiKey)
    agent.connect()

//    val result = agent.read("https://www.reddit.com/r/AuroraCO/comments/1r33pww/dog_runaway_from_petco_mississippi_and_havana_area/")
//    println(result)
}