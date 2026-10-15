package kampfire.api

import io.ktor.http.HttpMethod
import kampfire.utils.pascalToKebabCase

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
