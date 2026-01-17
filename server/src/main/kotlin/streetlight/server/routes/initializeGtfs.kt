package streetlight.server.routes

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import kabinet.console.globalConsole
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import streetlight.model.data.TransitRoute
import streetlight.model.data.TransitRouteId
import streetlight.model.data.TransitStop
import streetlight.model.data.TransitStopId
import streetlight.model.data.VehicleType
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import java.io.File
import java.util.zip.ZipInputStream
import kotlin.time.Duration.Companion.days

private val console = globalConsole.getHandle("initGtfs")
private val httpClient = HttpClient(CIO)

suspend fun initGtfs(app: ServerProvider = RuntimeProvider) {
    if (app.dao.transitRoute.readAllRoutes().isNotEmpty()) return
    console.log("initializin' gtfs")
    val cacheDir = File("gtfs")
    if (!cacheDir.exists()) cacheDir.mkdirs()

    val gtfsFile = File(cacheDir, "google_transit.zip")
    val oneMonth = 30.days

    val needsDownload = !gtfsFile.exists() ||
            (Clock.System.now() - Instant.fromEpochMilliseconds(gtfsFile.lastModified()) > oneMonth)

    val zipBytes = if (needsDownload) {
        console.log("fetchin' fresh gtfs from the horizon...")
        val url = "https://www.rtd-denver.com/files/gtfs/google_transit.zip"
        val upstreamResponse: HttpResponse = httpClient.get(url)
        val bytes = upstreamResponse.readRawBytes()
        gtfsFile.writeBytes(bytes)
        bytes
    } else {
        console.log("usin' the maps we already got in the hold.")
        gtfsFile.readBytes()
    }

    unzipSelected(zipBytes, setOf("routes.txt", "stops.txt")) { name, text ->
        if (name == "routes.txt") {
            val routes = parseCsv(text) {
                // route_id (0),agency_id (1),route_short_name (2),route_long_name (3),route_desc (4),route_type (5)
                // route_url,route_color,route_text_color
                // 0,RTD,0,Broadway,This Route Travels Northbound & Southbound,3,
                // http://www.rtd-denver.com/Schedules.shtml,0076CE,FFFFFF
                TransitRoute(
                    transitRouteId = TransitRouteId(it[0]),
                    shortName = it[2],
                    longName = it[3],
                    description = it[4].ifBlank { null },
                    vehicleType = when (it[5].toIntOrNull()) {
                        3 -> VehicleType.Bus
                        2 -> VehicleType.Train
                        0 -> VehicleType.LightRail
                        else -> null
                    }
                )
            }
            routes.forEach {
                app.dao.transitRoute.upsert(it)
            }
            console.log("found ${routes.size} routes")
        }
        if (name == "stops.txt") {
            val stops = parseCsv(text) {
                // stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,
                // parent_station,stop_timezone,wheelchair_boarding
                // 26199,26199,Nine Mile Station Gate J,Vehicles Travelling Southwest,39.657746,-104.846604,,,0,
                // 33719,,1
                TransitStop(
                    transitStopId = TransitStopId(it[0]),
                    name = it[2],
                    latitude = it[4].toDouble(),
                    longitude = it[5].toDouble(),
                    description = it[3].ifBlank { null }
                )
            }
            stops.forEach {
                app.dao.transitStop.upsert(it)
            }
            console.log("found ${stops.size} stops")
        }
    }
}

suspend fun unzipSelected(
    zipBytes: ByteArray,
    wanted: Set<String>,
    consume: suspend (name: String, data: String) -> Unit
) {
    ZipInputStream(zipBytes.inputStream()).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            if (!entry.isDirectory && entry.name in wanted) {
                val data = zip.readBytes().decodeToString()
                consume(entry.name, data)
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }
}

fun <T> parseCsv(
    csv: String,
    delimiter: Char = ',',
    hasHeader: Boolean = true,
    mapper: (List<String>) -> T
): List<T> {
    val lines = csv
        .lineSequence()
        .filter { it.isNotBlank() }
        .toList()

    val dataLines = if (hasHeader) lines.drop(1) else lines

    return dataLines.map { line ->
        val fields = line.split(delimiter)
        mapper(fields)
    }
}