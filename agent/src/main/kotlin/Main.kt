import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.OllamaModels
import klutch.environment.readEnvFromPath
import kotlinx.coroutines.runBlocking
import streetlight.agent.ChatAgentConnection
import streetlight.agent.UrlParser
import streetlight.model.data.EventParse
import streetlight.model.data.EventParseItem

fun main(): Unit = runBlocking {
    val env = readEnvFromPath()
    // connectDb(env)
    // Get an API key from the OPENAI_API_KEY environment variable
    val apiKey = env.read("GEMINI_KEY_A")

    // println(GoogleModels.Gemini2_5Flash.supports(LLMCapability.Vision.Image))
    val agent = UrlParser(apiKey)
    val result: EventParse? =
        agent.readImage("../upload/c285c6c0-0b15-4113-a1e9-0add48984ac9.jpg", eventInstructions)

     println(result)
//    val agent = ChatAgentConnection(apiKey)
//    agent.connect()

//    val result = agent.read("https://www.reddit.com/r/AuroraCO/comments/1r33pww/dog_runaway_from_petco_mississippi_and_havana_area/")
//    println(result)
}

val eventInstructions = """
        Read the following image. We believe it is information about an event or a list of events.
        For each event, your job is to extract the following json properties, if that information can be found in the content:
        
        * name: Event name or title 
        * time: Time of day of the event as 24-hour value [HH:MM]
        * date: Date of the event as ISO local date [YYYY-MM-DD]
        * location: The name or description of the location of the event
        * address: The address at which the event is located
        * imageUrl: The featured image for the event, must be a full url
        * description: Additional details given about the event
        * ageMin: The minimum age for attendees
        * contact: Any name and/or contact information given for the event
        * url: Url for more information about the event, must be a full url
""".trimIndent()