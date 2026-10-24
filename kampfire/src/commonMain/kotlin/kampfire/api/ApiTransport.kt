package kampfire.api

import kampfire.model.Outcome
import kotlinx.coroutines.flow.Flow

/** Carries calls to the API and opens its streams over a platform's HTTP. */
interface ApiTransport {
    /** Sends [call], returning its decoded response or a problem. */
    suspend fun <Sent, Returned> send(call: ApiCall<Sent, Returned>): Outcome<Returned>

    /** The text of each event the stream at [path] sends, open while collected. */
    fun openEvents(path: String): Flow<String>

    /** Opens a socket to [path]. */
    fun openSocket(path: String): ApiSocket
}

/** An open socket carrying text. */
interface ApiSocket {
    /** Each message received. */
    val messages: Flow<String>

    /** Sends [text], held until the socket is open. */
    fun send(text: String)

    fun close()
}
