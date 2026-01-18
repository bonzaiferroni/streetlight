package streetlight.web

import kabinet.api.GetEndpoint
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array

suspend inline fun <reified T> GetEndpoint<T>.get(): T {
    val response = window.fetch(path).await()
    val text = response.text().await()
    return Json.decodeFromString(text)
}

suspend inline fun <reified T> GetEndpoint<Unit>.getProtobuf(feedType: ProtobufType): FeedMessage<T> {
    val response = window.fetch(path).await()
        .arrayBuffer().await()
    val buffer = Uint8Array(response)

    return feedType.decode(buffer)
}