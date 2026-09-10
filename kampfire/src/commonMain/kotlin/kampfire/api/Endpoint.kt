package kampfire.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import kampfire.utils.pascalToKebabCase
import kotlin.enums.enumEntries
import kotlin.time.Instant
import kotlin.uuid.Uuid

abstract class Endpoint<SentType, ReturnType>(
    val method: HttpMethod?,
    val parent: Endpoint<*, *>?,
    pathNode: String?,
) {
    val pathNode = pathNode ?: this::class.simpleName!!.pascalToKebabCase()
    val pathSegments: List<String> = createPathSegments()
    val path: String = "/${pathSegments.joinToString("/")}"
    val serverIdTemplate: String get() = "$path/{id}"

    fun pathWithId(id: String) = "$path/$id"

    private fun createPathSegments(): MutableList<String> = (parent?.createPathSegments() ?: mutableListOf()).also {
        it.add(pathNode)
    }

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

    inline fun <reified T : Enum<T>> enumParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { enumEntries<T>()[it.toInt()] },
        toString = { (it as Enum<T>).ordinal.toString() }
    )

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

    fun stringOrNullParamOf(key: String) = EndpointParam<String?>(
        key = key,
        toValue = { it },
        toString = { it ?: error("value not found") },
    )

    fun uuidParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { Uuid.parse(it) },
        toString = { it.toString() }
    )

    fun booleanParamOf(key: String) = EndpointParam(
        key = key,
        toValue = { it.toBoolean() },
        toString = { it.toString() }
    )

    fun intListParamOf(key: String) = listParamOf(key, { it.toInt()}, { it.toString() })

    fun <T> listParamOf(key: String, toValue: (String) -> T, toString: (T) -> String) = EndpointParam<Collection<T>>(
        key = key,
        toValue = { it.takeIf { it.isNotEmpty() }?. split(",")?.map { toValue(it)} ?: emptyList() },
        toString = { it.joinToString(",") { toString(it)} }
    )
}

open class ApiNode(
    parent: Endpoint<*, *>? = null,
    pathNode: String? = null,
): Endpoint<Unit, Unit>(null, parent, pathNode)

open class GetEndpoint<Returned>(
    parent: Endpoint<*, *>? = null,
    pathNode: String? = null,
): Endpoint<Unit, Returned>(HttpMethod.Get, parent, pathNode)

open class QueryEndpoint<Sent, Returned>(
    parent: Endpoint<*, *>? = null,
    pathNode: String? = null,
): Endpoint<Sent, Returned>(HttpMethod.Get, parent, pathNode)

open class PostEndpoint<Sent, Returned>(
    parent: Endpoint<*,*>? = null,
    pathNode: String? = null,
): Endpoint<Sent, Returned>(HttpMethod.Post, parent, pathNode)

open class GetByIdEndpoint<Sent, Returned>(
    parent: Endpoint<*, *>? = null,
    pathNode: String? = null,
) : Endpoint<Sent, Returned>(HttpMethod.Get, parent, pathNode) {
    val clientIdTemplate: String get() = "$path/:id"
    fun replaceClientId(id: Any) = this.clientIdTemplate.replace(":id", id.toString())
}

open class DeleteEndpoint<Sent>(
    parent: Endpoint<*,*>? = null,
    pathNode: String? = null,
) : Endpoint<Sent, Boolean>(HttpMethod.Delete, parent, pathNode)

open class UpdateEndpoint<Sent>(
    parent: Endpoint<*,*>? = null,
    pathNode: String? = null,
) : Endpoint<Sent, Boolean>(HttpMethod.Put, parent, pathNode)

open class SocketDaoEndpoint<Item, ItemId, NewItem>(
    parent: Endpoint<*,*>? = null,
    pathNode: String? = null,
): Endpoint<Unit, Unit>(null, parent, pathNode)

class EndpointParam<T>(
    val key: String,
    val toValue: (String) -> T,
    val toString: (T) -> String,
) {
    fun write(value: T) = value.let { key to toString(value) }
    fun read(str: String) = toValue(str)
}

fun <T> HttpRequestBuilder.write(param: EndpointParam<T>, value: T?) {
    value?.let { parameter(param.key, param.toString(it)) }
}

class PathBuilder(
    private val basePath: String
) {
    private var params: MutableList<Pair<String, String>> = mutableListOf()

    fun <T> writeParam(param: EndpointParam<T>, value: T?) {
        if (value == null) {
            return
        }
        params.add(param.write(value))
    }

    fun build(): String {
        return if (params.isNotEmpty()) {
            "$basePath?${params.joinToString("&") { "${it.first}=${it.second}" }}"
        } else {
            basePath
        }
    }
}



