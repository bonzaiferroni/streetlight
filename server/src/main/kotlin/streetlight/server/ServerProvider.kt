package streetlight.server

import kabinet.clients.ReplicateClient
import kabinet.clients.ReplicateInput
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
    val songPlay: RenditionTableDao = RenditionTableDao(),
    val user: UserTableDao = UserTableDao(),
    val spark: SparkTableDao = SparkTableDao(),
    val request: RequestTableDao = RequestTableDao(),
)

class ServerService(
    val song: SongTableService = SongTableService(),
    val service: UserTableService = UserTableService(),
)

object RuntimeProvider: ServerProvider {
    override val env = readEnvFromPath()

    private val replicate = ReplicateClient(env.read("REPLICATE_KEY"))
    private val serverIp = env.read("SERVER_IP")
    private val speechPort = env.read("SERVER_PORT")

    override val dao = ServerDao()
    override val service = ServerService()
    override val gemini = GeminiService(env)
    override val speech = SpeechService { request ->
        replicate.requestBytes(
            // model = "lucataco/orpheus-3b-0.1-ft:79f2a473e6a9720716a473d9b2f2951437dbf91dc02ccb7079fb3d89b881207f",
            // url = "http://localhost:5000/predictions",
            url = "http://$serverIp:$speechPort/speech",
            input = ReplicateInput(
                text = request.text,
                voice = request.voice
            )
        )
    }
}