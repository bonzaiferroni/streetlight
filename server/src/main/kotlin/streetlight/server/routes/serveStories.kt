package streetlight.server.routes

import io.ktor.server.routing.Routing
import kabinet.console.globalConsole
import klutch.environment.readEnvFromPath
import klutch.server.getEndpoint
import klutch.server.readParam
import streetlight.agent.UrlParser
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

private val console = globalConsole.getHandle(Routing::serveStories.name)

fun Routing.serveStories(app: ServerProvider = RuntimeProvider) {
    val env = readEnvFromPath()
    // Get an API key from the OPENAI_API_KEY environment variable
    val apiKey = env.read("GEMINI_KEY_A")
    val agent = UrlParser(apiKey)

    getEndpoint(Api.Stories.ReadUrl) { endpoint ->
        val url = readParam(endpoint.url)
        agent.read(url = url)
    }
}