package kampfire.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.http.formUrlEncode
import kampfire.model.GeoRect
import kampfire.model.toArrayString
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A query parameter of an [Endpoint] or a route, with the conversions between its value and its text. */
class QueryParam<T>(
    val key: String,
    val toValue: (String) -> T,
    val toString: (T) -> String,
) {
    /** The parameter as a key and text pair. */
    fun write(value: T) = value.let { key to toString(value) }
    fun read(str: String) = toValue(str)
}

/** Declares a query parameter under [key], read and written with [toValue] and [toString]. */
fun <T> paramOf(key: String, toValue: (String) -> T, toString: (T) -> String, ) =
    QueryParam<T>(key, toValue, toString)

fun longParamOf(key: String) = QueryParam(
    key = key,
    toValue = { it.toLong() },
    toString = { it.toString() }
)

fun intParamOf(key: String) = QueryParam(
    key = key,
    toValue = { it.toInt() },
    toString = { it.toString() }
)

fun doubleParamOf(key: String) = QueryParam(
    key = key,
    toValue = { it.toDouble() },
    toString = { it.toString() }
)

/** Declares an enum query parameter, written as the entry's name. */
inline fun <reified T : Enum<T>> enumParamOf(key: String) = QueryParam(
    key = key,
    toValue = { enumValueOf<T>(it) },
    toString = { (it as Enum<T>).name }
)

/** Declares an [Instant] query parameter, written in epoch seconds. */
fun instantParamOf(key: String) = QueryParam(
    key = key,
    toValue = { Instant.fromEpochSeconds(it.toLong())},
    toString = { it.epochSeconds.toString() }
)

fun stringParamOf(key: String) = QueryParam(
    key = key,
    toValue = { it },
    toString = { it },
)

fun usernameParamOf(key: String) = QueryParam(
    key = key,
    toValue = { it.toUsername() },
    toString = { it.value },
)

/** Declares a nullable string query parameter. A `null` value is never written. */
fun stringOrNullParamOf(key: String) = QueryParam<String?>(
    key = key,
    toValue = { it },
    toString = { it ?: error("string param $key not found") },
)

fun uuidParamOf(key: String) = QueryParam(
    key = key,
    toValue = { Uuid.parse(it) },
    toString = { it.toString() }
)

/** Declares a query parameter for a record id, read with [toValue] from the [Uuid]. */
fun <T: TableId<Uuid>> tableIdParamOf(key: String, toValue: (Uuid) -> T) = QueryParam(
    key = key,
    toValue = { toValue(Uuid.parse(it)) },
    toString = { it.value.toString() }
)

fun booleanParamOf(key: String) = QueryParam(
    key = key,
    toValue = { it.toBoolean() },
    toString = { it.toString() }
)

fun geoRectParamOf(key: String) = QueryParam(
    key = key,
    toValue = { GeoRect.of(it) },
    toString = { it.toString() }
)

/** Declares a query parameter holding a list of [GeoRect]s. */
fun geoRectArrayOf(key: String) = QueryParam(
    key = key,
    toValue = { GeoRect.arrayOf(it) },
    toString = { it?.toArrayString() ?: error("rect array param $key not found") }
)

fun intListParamOf(key: String) = listParamOf(key, { it.toInt()}, { it.toString() })

/** Declares a query parameter holding a list, written comma-separated. */
fun <T> listParamOf(key: String, toValue: (String) -> T, toString: (T) -> String) = QueryParam<Collection<T>>(
    key = key,
    toValue = { it.takeIf { it.isNotEmpty() }?. split(",")?.map { toValue(it)} ?: emptyList() },
    toString = { it.joinToString(",") { toString(it)} }
)

/** Adds [param] to the request's query string, or nothing when [value] is `null`. */
fun <T> HttpRequestBuilder.write(param: QueryParam<T>, value: T?) {
    value?.let { parameter(param.key, param.toString(it)) }
}

/** The value of [param] in these parameters, or `null` when absent. */
fun <T> Parameters.read(param: QueryParam<T>) = this[param.key]?.let(param::read)

/** Builds a path from [basePath] and the parameters written to it. */
class PathBuilder(
    private val basePath: String
) {
    private val params = ParametersBuilder()

    /** Adds [param] with [value] to the query string. A `null` value is skipped. */
    fun <T> writeParam(param: QueryParam<T>, value: T?) {
        value?.let { params.append(param.key, param.toString(it)) }
    }

    /** The base path followed by the encoded query string of the parameters written so far. */
    fun build(): String {
        val query = params.build().formUrlEncode()
        return if (query.isEmpty()) basePath else "$basePath?$query"
    }
}
