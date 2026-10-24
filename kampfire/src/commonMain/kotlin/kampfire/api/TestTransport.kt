package kampfire.api

import kampfire.model.Outcome
import kampfire.model.OutcomeSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

/**
 * An [ApiTransport] for tests, answering each endpoint with the block given to [answer].
 *
 * A JSON body and each answer pass through JSON on the way, so a type that does not serialize fails the test.
 * A call to an endpoint with no answer fails, naming its path.
 */
class TestTransport(configure: TestTransport.() -> Unit = {}) : ApiTransport {
    private val answers = mutableMapOf<Endpoint<*, *>, suspend (ApiCall<*, *>) -> Outcome<*>>()

    init {
        configure()
    }

    /** Answers each call to [endpoint] with [block]. A later answer for the same endpoint replaces it. */
    @Suppress("UNCHECKED_CAST")
    fun <Sent, Returned> answer(
        endpoint: Endpoint<Sent, Returned>,
        block: suspend (ApiCall<Sent, Returned>) -> Outcome<Returned>,
    ) {
        answers[endpoint] = block as suspend (ApiCall<*, *>) -> Outcome<*>
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <Sent, Returned> send(call: ApiCall<Sent, Returned>): Outcome<Returned> {
        val answer = answers[call.endpoint] ?: error("TestTransport has no answer for ${call.endpoint.path}")

        val sentSerializer = call.sentSerializer
        val sent = call.sent
        val passedCall = when {
            sentSerializer == null || sent == null -> call
            else -> call.withSent(json.decodeFromString(sentSerializer, json.encodeToString(sentSerializer, sent)))
        }
        val outcome = answer(passedCall) as Outcome<Returned>

        val outcomeSerializer = OutcomeSerializer(call.returnedSerializer)
        return json.decodeFromString(outcomeSerializer, json.encodeToString(outcomeSerializer, outcome))
    }

    override fun openEvents(path: String): Flow<String> = TODO("TestTransport has no events for $path")

    override fun openSocket(path: String): ApiSocket = TODO("TestTransport has no socket for $path")
}

private val json = Json
