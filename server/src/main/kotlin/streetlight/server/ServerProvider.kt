package streetlight.server

import kabinet.clients.ReplicateClient
import kabinet.clients.ReplicateInput
import kabinet.console.globalConsole
import kabinet.utils.Environment
import klutch.db.services.UserTableDao
import klutch.db.services.UserTableService
import klutch.environment.readEnvFromPath
import klutch.gemini.GeminiService
import klutch.gemini.SpeechService
import streetlight.server.db.services.AreaTableDao
import streetlight.server.db.services.EventTableDao
import streetlight.server.db.services.LocationTableDao
import streetlight.server.db.services.SongTableDao
import streetlight.server.db.services.RenditionTableDao
import streetlight.server.db.services.SongTableService
import streetlight.server.db.services.SparkTableDao
import streetlight.server.db.services.RequestTableDao
import streetlight.server.db.services.PerformerTableDao
import streetlight.server.db.services.TransitRouteTableDao
import streetlight.server.db.services.TransitStopTableDao
import streetlight.server.db.services.UserFileTableDao

interface ServerProvider {
    val env: Environment
    val dao: ServerDao
    val service: ServerService
    val gemini: GeminiService
    val speech: SpeechService
}

class ServerDao(
    val location: LocationTableDao = LocationTableDao(),
    val area: AreaTableDao = AreaTableDao(),
    val song: SongTableDao = SongTableDao(),
    val event: EventTableDao = EventTableDao(),
    val rendition: RenditionTableDao = RenditionTableDao(),
    val user: UserTableDao = UserTableDao(),
    val spark: SparkTableDao = SparkTableDao(),
    val request: RequestTableDao = RequestTableDao(),
    val performer: PerformerTableDao = PerformerTableDao(),
    val transitRoute: TransitRouteTableDao = TransitRouteTableDao(),
    val transitStop: TransitStopTableDao = TransitStopTableDao(),
    val userFile: UserFileTableDao = UserFileTableDao()
)

class ServerService(
    val song: SongTableService = SongTableService(),
    val service: UserTableService = UserTableService(),
)

private val console = globalConsole.getHandle(RuntimeProvider::class)

object RuntimeProvider: ServerProvider {
    override val env = readEnvFromPath()

    private val replicate = ReplicateClient(env.read("REPLICATE_KEY"))
    private val serverIp = env.read("SERVER_IP")
    private val speechPort = env.read("SERVER_PORT")

    private val failedRequestFilter = mutableSetOf<String>()

    override val dao = ServerDao()
    override val service = ServerService()
    override val gemini = GeminiService(env)
    override val speech = SpeechService { request ->
        val filename = request.toFilename()
        if (failedRequestFilter.contains(filename)) return@SpeechService null
        val model = "lucataco/orpheus-3b-0.1-ft:79f2a473e6a9720716a473d9b2f2951437dbf91dc02ccb7079fb3d89b881207f"
        val request = ReplicateInput(
            text = request.text,
            voice = request.voice,
            maxNewTokens = 2000
        )
        replicate.requestBytes(
            // model = "lucataco/orpheus-3b-0.1-ft:79f2a473e6a9720716a473d9b2f2951437dbf91dc02ccb7079fb3d89b881207f",
//             url = "http://localhost:5000/predictions",
            url = "http://$serverIp:$speechPort/speech",
            input = request
        ).also {
            if (it == null) console.log("holocene unable to generate speech")
        } ?: replicate.requestFileBytes(model, request).also {
            if (it == null) console.log("replicate unable to generate speech").also { failedRequestFilter.add(filename)}
        }

//        replicate.requestFileBytes(model, request)
    }
}