package kampfire.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import kotlinx.datetime.Instant

abstract class Endpoint<SentType, ReturnType>(
    val method: HttpMethod?,
    val parent: Endpoint<*, *>?,
    val pathNode: String,
    val appendId: Boolean = false,
) {
    val pathSegments: List<String> = createPathSegments()
    val path: String = "/${pathSegments.joinToString("/")}"
    val serverIdTemplate: String get() = "$path/{id}"

    private fun createPathSegments(): MutableList<String> = (parent?.createPathSegments() ?: mutableListOf()).also {
        it.add(pathNode)
    }

    fun <T> addParam(key: String, toValue: (String) -> T, toString: (T) -> String, ) =
        EndpointParam<T>(key, toValue, toString)

    fun addLongParam(key: String) = EndpointParam(
        key = key,
        toValue = { it.toLong() },
        toString = { it.toString() }
    )

    fun addIntParam(key: String) = EndpointParam(
        key = key,
        toValue = { it.toInt() },
        toString = { it.toString() }
    )

    fun addInstantParam(key: String) = EndpointParam(
        key = key,
        toValue = { Instant.fromEpochSeconds(it.toLong())},
        toString = { it.epochSeconds.toString() }
    )

    fun addStringParam(key: String) = EndpointParam(
        key = key,
        toValue = { it },
        toString = { it }
    )

    fun addBooleanParam(key: String) = EndpointParam(
        key = key,
        toValue = { it.toBoolean() },
        toString = { it.toString() }
    )

    fun addIntList(key: String) = addListParam(key, { it.toInt()}, { it.toString() })

    fun <T> addListParam(key: String, toValue: (String) -> T, toString: (T) -> String) = EndpointParam<Collection<T>>(
        key = key,
        toValue = { it.takeIf { it.isNotEmpty() }?. split(",")?.map { toValue(it)} ?: emptyList() },
        toString = { it.joinToString(",") { toString(it)} }
    )
}

open class ApiNode(
    parent: Endpoint<*, *>? = null,
    pathNode: String,
): Endpoint<Unit, Unit>(null, parent, pathNode)

open class GetEndpoint<Returned>(
    parent: Endpoint<*, *>? = null,
    pathNode: String = "",
): Endpoint<Unit, Returned>(HttpMethod.Get, parent, pathNode)

open class QueryEndpoint<Sent, Returned>(
    parent: Endpoint<*, *>? = null,
    pathNode: String = "",
): Endpoint<Sent, Returned>(HttpMethod.Get, parent, pathNode)

open class PostEndpoint<Sent, Returned>(
    parent: Endpoint<*,*>? = null,
    pathNode: String = "",
): Endpoint<Sent, Returned>(HttpMethod.Post, parent, pathNode)

open class PostObjectEndpoint<Returned>(
    parent: Endpoint<*,*>? = null,
    pathNode: String = "",
): Endpoint<Unit, Returned>(HttpMethod.Post, parent, pathNode)

open class GetByIdEndpoint<Returned>(
    parent: Endpoint<*, *>? = null,
    pathNode: String = "",
) : Endpoint<Unit, Returned>(HttpMethod.Get, parent, pathNode, true) {
    val clientIdTemplate: String get() = "$path/:id"
    fun replaceClientId(id: Any) = this.clientIdTemplate.replace(":id", id.toString())
}

open class GetByTableIdEndpoint<Id: TableId<*>, Returned>(
    parent: Endpoint<*,*>? = null,
    pathNode: String = "",
): Endpoint<Returned, Unit>(HttpMethod.Get, parent, pathNode, true) {
    val clientIdTemplate: String get() = "$path/:id"
    fun replaceClientId(id: Id) = this.clientIdTemplate.replace(":id", id.value.toString())
}

open class DeleteEndpoint<Sent>(
    parent: Endpoint<*,*>? = null,
    pathNode: String = "",
) : Endpoint<Sent, Boolean>(HttpMethod.Delete, parent, pathNode)

open class UpdateEndpoint<Sent>(
    parent: Endpoint<*,*>? = null,
    pathNode: String = "",
) : Endpoint<Sent, Boolean>(HttpMethod.Put, parent, pathNode)

open class SocketDaoEndpoint<Item, ItemId, NewItem>(
    parent: Endpoint<*,*>? = null,
    pathNode: String = "",
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

class ApiRequestBuilder<E: Endpoint<*,*>>(
    val endpoint: E
) {
    var params: List<Pair<String, String>>? = null

    fun setParams(vararg params: Pair<String, String>) {
        this.params = params.asList()
    }
}

class PathBuilder(
    private val endpoint: Endpoint<*,*>
) {
    private var params: MutableList<Pair<String, String>> = mutableListOf()

    fun <T> param(param: EndpointParam<T>, value: T) {
        params.add(param.write(value))
    }

    fun build(): String {
        return if (params.isNotEmpty()) {
            "${endpoint.path}?${params.joinToString("&") { "${it.first}=${it.second}" }}"
        } else {
            endpoint.path
        }
    }
}