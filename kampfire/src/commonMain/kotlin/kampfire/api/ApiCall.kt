package kampfire.api

import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.formUrlEncode
import kotlinx.serialization.KSerializer

/**
 * A call to [endpoint], ready for an [ApiTransport] to send.
 *
 * [sent] is the id of a [GetByIdEndpoint] or the body of a POST, which is sent as JSON when [sentSerializer] is
 * given. [form] replaces the body with a multipart form.
 */
class ApiCall<Sent, Returned>(
    val endpoint: Endpoint<Sent, Returned>,
    val returnedSerializer: KSerializer<Returned>,
    val sent: Sent? = null,
    val sentSerializer: KSerializer<Sent>? = null,
    val parameters: Parameters = Parameters.Empty,
    val form: List<FormPart>? = null,
) {
    val method: HttpMethod get() = endpoint.method ?: error("${endpoint.path} is not callable")

    /** The endpoint's path, with the id as its last segment and the parameters as its query string. */
    val path: String get() {
        val base = if (endpoint is GetByIdEndpoint<*, *>) endpoint.pathWithId(sent.toString()) else endpoint.path
        val query = parameters.formUrlEncode()
        return if (query.isEmpty()) base else "$base?$query"
    }

    /** The value of [param] in the query, or `null` when it was not written. */
    fun <T> param(param: QueryParam<T>): T? = parameters[param.key]?.let(param::read)

    /** This call with [sent] in place of its own. */
    fun withSent(sent: Sent?) = ApiCall(endpoint, returnedSerializer, sent, sentSerializer, parameters, form)
}

/** A part of a multipart form. */
sealed interface FormPart {
    val name: String
}

/** A text part, with its [contentType] when it is not plain text. */
data class TextPart(
    override val name: String,
    val value: String,
    val contentType: String? = null,
): FormPart

/** A file part named [fileName]. */
class FilePart(
    override val name: String,
    val bytes: ByteArray,
    val fileName: String,
): FormPart
