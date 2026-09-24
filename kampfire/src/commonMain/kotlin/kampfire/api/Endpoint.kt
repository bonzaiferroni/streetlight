package kampfire.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import kampfire.model.GeoRect
import kampfire.model.toArrayString
import kampfire.utils.pascalToKebabCase
import kotlin.enums.enumEntries
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * A node in an API tree, with its HTTP [method] and a [path] built from its ancestors.
 *
 * The path segment defaults to the class name in kebab case. An endpoint declares its query parameters with the
 * `fooParamOf` factories.
 */
abstract class Endpoint<SentType, ReturnType>(
    val method: HttpMethod?,
    val parent: Endpoint<*, *>?,
    pathNode: String?,
) {
    val pathNode = pathNode ?: this::class.simpleName!!.pascalToKebabCase()
    val pathSegments: List<String> = createPathSegments()
    val path: String = "/${pathSegments.joinToString("/")}"
    /** The path with an `{id}` segment, in the form the server's routing reads. */
    val serverIdTemplate: String get() = "$path/{id}"

    /** The path followed by [id] as its last segment. */
    fun pathWithId(id: String) = "$path/$id"

    private fun createPathSegments(): MutableList<String> = (parent?.createPathSegments() ?: mutableListOf()).also {
        it.add(pathNode)
    }

    /** Declares a query parameter under [key], read and written with [toValue] and [toString]. */
    fun <T> paramOf(key: String, toValue: (String) -> T, toString: (T) -> String, ) =
        EndpointParam<T>(key, toValue, toString)

    fun longParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { it.toLong() },
        toString = { it.toString() }
    )

    fun intParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { it.toInt() },
        toString = { it.toString() }
    )

    /** Declares an enum query parameter, written as the entry's ordinal. */
    inline fun <reified T : Enum<T>> enumParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { enumEntries<T>()[it.toInt()] },
        toString = { (it as Enum<T>).ordinal.toString() }
    )

    /** Declares an [Instant] query parameter, written in epoch seconds. */
    fun instantParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { Instant.fromEpochSeconds(it.toLong())},
        toString = { it.epochSeconds.toString() }
    )

    fun stringParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { it },
        toString = { it },
    )

    fun usernameParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { it.toUsername() },
        toString = { it.value },
    )

    /** Declares a nullable string query parameter. A `null` value is never written. */
    fun stringOrNullParamOf(key: String) = EndpointParam<String?>(
        key = key,
        toValue = { it },
        toString = { it ?: error("string param $key not found") },
    )

    fun uuidParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { Uuid.parse(it) },
        toString = { it.toString() }
    )

    /** Declares a query parameter for a record id, read with [toValue] from the [Uuid]. */
    fun <T: TableId<Uuid>> tableIdParamOf(key: String, toValue: (Uuid) -> T) = EndpointParam(
        key = key,
        toValue = { toValue(Uuid.parse(it)) },
        toString = { it.value.toString() }
    )

    fun booleanParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { it.toBoolean() },
        toString = { it.toString() }
    )

    fun geoRectOf(key: String) = EndpointParam(
        key = key,
        toValue = { GeoRect.of(it) },
        toString = { it.toString() }
    )

    /** Declares a query parameter holding a list of [GeoRect]s. */
    fun geoRectArrayOf(key: String) = EndpointParam(
        key = key,
        toValue = { GeoRect.arrayOf(it) },
        toString = { it?.toArrayString() ?: error("rect array param $key not found") }
    )

    fun intListParamOf(key: String) = listParamOf(key, { it.toInt()}, { it.toString() })

    /** Declares a query parameter holding a list, written comma-separated. */
    fun <T> listParamOf(key: String, toValue: (String) -> T, toString: (T) -> String) = EndpointParam<Collection<T>>(
        key = key,
        toValue = { it.takeIf { it.isNotEmpty() }?. split(",")?.map { toValue(it)} ?: emptyList() },
        toString = { it.joinToString(",") { toString(it)} }
    )
}

/** A node that groups endpoints under a path segment and is not called itself. */
open class ApiNode(
    parent: Endpoint<*, *>? = null,
    pathNode: String? = null,
): Endpoint<Unit, Unit>(null, parent, pathNode)

/** A `GET` endpoint that sends no body and returns [Returned]. */
open class GetEndpoint<Returned>(
    parent: Endpoint<*, *>? = null,
    pathNode: String? = null,
): Endpoint<Unit, Returned>(HttpMethod.Get, parent, pathNode)

/** A `GET` endpoint that sends [Sent] as a query string and returns [Returned]. */
open class QueryEndpoint<Sent, Returned>(
    parent: Endpoint<*, *>? = null,
    pathNode: String? = null,
): Endpoint<Sent, Returned>(HttpMethod.Get, parent, pathNode)

/** A `POST` endpoint that sends [Sent] as its body and returns [Returned]. */
open class PostEndpoint<Sent, Returned>(
    parent: Endpoint<*,*>? = null,
    pathNode: String? = null,
): Endpoint<Sent, Returned>(HttpMethod.Post, parent, pathNode)

/** A `GET` endpoint that sends [Sent] as the last path segment and returns [Returned]. */
open class GetByIdEndpoint<Sent, Returned>(
    parent: Endpoint<*, *>? = null,
    pathNode: String? = null,
) : Endpoint<Sent, Returned>(HttpMethod.Get, parent, pathNode) {
    /** The path with a `:id` segment, in the form the client's router reads. */
    val clientIdTemplate: String get() = "$path/:id"
    /** The client path with [id] in place of `:id`. */
    fun replaceClientId(id: Any) = this.clientIdTemplate.replace(":id", id.toString())
}

/** A `DELETE` endpoint that sends [Sent] and returns whether anything was deleted. */
open class DeleteEndpoint<Sent>(
    parent: Endpoint<*,*>? = null,
    pathNode: String? = null,
) : Endpoint<Sent, Boolean>(HttpMethod.Delete, parent, pathNode)

/** A `PUT` endpoint that sends [Sent] and returns whether anything was updated. */
open class UpdateEndpoint<Sent>(
    parent: Endpoint<*,*>? = null,
    pathNode: String? = null,
) : Endpoint<Sent, Boolean>(HttpMethod.Put, parent, pathNode)

/** A websocket endpoint that streams changes to a collection of [Item]s. */
open class SocketDaoEndpoint<Item, ItemId, NewItem>(
    parent: Endpoint<*,*>? = null,
    pathNode: String? = null,
): Endpoint<Unit, Unit>(null, parent, pathNode)

/** A query parameter of an [Endpoint], with the conversions between its value and its text. */
class EndpointParam<T>(
    val key: String,
    val toValue: (String) -> T,
    val toString: (T) -> String,
) {
    /** The parameter as a key and text pair. */
    fun write(value: T) = value.let { key to toString(value) }
    fun read(str: String) = toValue(str)
}

/** Adds [param] to the request's query string, or nothing when [value] is `null`. */
fun <T> HttpRequestBuilder.write(param: EndpointParam<T>, value: T?) {
    value?.let { parameter(param.key, param.toString(it)) }
}

/** Builds a request path from [basePath] and the parameters written to it. */
class PathBuilder(
    private val basePath: String
) {
    private var params: MutableList<Pair<String, String>> = mutableListOf()

    /** Adds [param] with [value] to the query string. A `null` value is skipped. */
    fun <T> writeParam(param: EndpointParam<T>, value: T?) {
        if (value == null) {
            return
        }
        params.add(param.write(value))
    }

    /** The base path followed by the query string of the parameters written so far. */
    fun build(): String {
        return if (params.isNotEmpty()) {
            "$basePath?${params.joinToString("&") { "${it.first}=${it.second}" }}"
        } else {
            basePath
        }
    }
}



