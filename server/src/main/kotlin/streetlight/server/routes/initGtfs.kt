package streetlight.server.routes

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import kabinet.console.globalConsole
import kabinet.model.GeoPoint
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import streetlight.model.data.TransitRoute
import streetlight.model.data.TransitRouteId
import streetlight.model.data.TransitShape
import streetlight.model.data.TransitStop
import streetlight.model.data.TransitStopTime
import streetlight.model.data.TransitTrip
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

    var routes: List<TransitRoute>? = null
    var stops: List<TransitStop>? = null
    var stopTimes: List<TransitStopTime>? = null
    var trips: List<TransitTrip>? = null
    var shapes: List<TransitShape>? = null
    unzipSelected(zipBytes, setOf("routes.txt", "stops.txt", "stop_times.txt", "trips.txt", "shapes.txt")) { name, text ->
        if (name == "routes.txt") {
            routes = parseCsv(text) { TransitRoute.fromCsv(it) }
            console.log("found ${routes.size} routes")
        }
        if (name == "stops.txt") {
            stops = parseCsv(text) { TransitStop.fromCsv(it) }
            console.log("found ${stops.size} stops")
        }
        if (name == "stop_times.txt") {
            stopTimes = parseCsv(text) { TransitStopTime.fromCsv(it) }
            console.log("found ${stopTimes.size} stop times")
        }
        if (name == "trips.txt") {
            trips = parseCsv(text) { TransitTrip.fromCsv(it) }
            console.log("found ${trips.size} trips")
        }
        if (name == "shapes.txt") {
            shapes = parseCsv(text) { TransitShape.fromCsv(it) }
            console.log("found ${shapes.size} shapes")
        }
    }

    requireNotNull(routes)
    requireNotNull(stops)
    requireNotNull(stopTimes)
    requireNotNull(trips)
    requireNotNull(shapes)

    val shapeLists = shapes.groupBy { it.shapeId }
    val routeTrips = routes.associate { route ->
        route.transitRouteId to trips.filter { it.transitRouteId == route.transitRouteId }.toSet()
    }

    val routesModified = routes.map { route ->
        val rt = routeTrips.getValue(route.transitRouteId)
        val maxShapeTrip = rt.maxBy { shapeLists[it.shapeId]?.size ?: 0 }
        val maxShapeList = shapeLists.getValue(maxShapeTrip.shapeId)
        route.copy(
            points = maxShapeList.map { GeoPoint(it.longitude, it.latitude) }
        )
    }

    console.log("upserting ${routesModified.size} routes")
    app.dao.transitRoute.batchUpsert(routesModified)
    console.log("upserting ${stops.size} size")
    app.dao.transitStop.batchUpsert(stops)

    val routeStops = routeTrips.entries.associate { (routeId, trips) ->
        routeId to stopTimes.filter { stopTime -> trips.any { stopTime.tripId == it.tripId } }.map { it.transitStopId }.toSet()
    }

    console.log("upserting routeStops for ${routeStops.size} routes")
    routeStops.forEach { (transitRouteId, transitStopIds) ->
        app.dao.transitRoute.upsertRouteStops(transitRouteId, transitStopIds)
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