package streetlight.web

import kampfire.api.GetEndpoint
import kampfire.api.QueryEndpoint
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array

suspend inline fun <reified Returned> GetEndpoint<Returned>.get(): Returned {
    val response = window.fetch(path).await()
    val text = response.text().await()
    return Json.decodeFromString(text)
}

suspend inline fun <reified Returned> GetEndpoint<Unit>.getProtobuf(feedType: ProtobufType): FeedMessage<Returned> {
    val response = window.fetch(path).await()
        .arrayBuffer().await()
    val buffer = Uint8Array(response)

    return feedType.decode(buffer)
}

suspend inline fun <reified Sent, reified Returned> QueryEndpoint<Sent, Returned>.get(
    query: String
): Returned {
    val url = if (query.isNotEmpty()) "$path?$query" else path
    val response = window.fetch(url).await()
    val text = response.text().await()
    return Json.decodeFromString(text)
}