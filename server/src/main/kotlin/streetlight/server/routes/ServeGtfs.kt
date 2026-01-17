package streetlight.server.routes

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import kotlin.time.Duration.Companion.seconds

private val httpClient = HttpClient(CIO)
private var vehiclePositionBytes: ByteArray? = null
private var contentType: ContentType = ContentType.Application.OctetStream
private var timestamp: Instant = Instant.DISTANT_PAST

fun Routing.serveGtfs(app: ServerProvider = RuntimeProvider) {
    CoroutineScope(Dispatchers.IO).launch {
        initGtfs(app)
    }

    get("/gtfs/vehicle-position.pb") {
        val bytes = if (vehiclePositionBytes != null && Clock.System.now() - timestamp < 10.seconds) {
            vehiclePositionBytes!!
        } else {
            val url = "https://open-data.rtd-denver.com/files/gtfs-rt/rtd/VehiclePosition.pb"
            val upstreamResponse: HttpResponse = httpClient.get(url)
            contentType = upstreamResponse.headers[HttpHeaders.ContentType]
                ?.let { ContentType.parse(it) }
                ?: ContentType.Application.OctetStream
            upstreamResponse.readRawBytes().also {
                vehiclePositionBytes = it
                timestamp = Clock.System.now()
            }
        }

        call.respondBytes(
            bytes = bytes,
            contentType = contentType
        )
    }

    get("/gtfs/routes") {
        val routeIds = setOf("15L", "15", "121", "121L", "107R", "101H", "228A")
        val routes = app.dao.transitRoute.readRoutes(routeIds)

        call.respond(routes)
    }
}