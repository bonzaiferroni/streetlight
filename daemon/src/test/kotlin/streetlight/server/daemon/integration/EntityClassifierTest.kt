package streetlight.server.daemon.integration

import kampfire.model.toDataOr
import kotlinx.coroutines.runBlocking
import streetlight.server.daemon.agent.EntityClassifier
import streetlight.server.daemon.agent.KoogEmbeddingsClient
import streetlight.server.daemon.agent.readClassifiedEvents
import streetlight.server.daemon.raiseEntityClassifier
import kotlin.test.Test
import kotlin.test.assertTrue

class EntityClassifierTest {

    @Test
    fun `observed events are given their expected type`() = runBlocking {
        val classifiedEvents = readClassifiedEvents("classified-events.json")

        val misses = classifiedEvents.mapNotNull { (event, expected, _) ->
            val given = classifier.classifyEvent(event).eventType
            if (given == expected) null else "${event.title}: expected $expected, given $given"
        }

        assertTrue(misses.isEmpty(), "every event should be given its expected type:\n${misses.joinToString("\n")}")
    }

    companion object {
        /** The classifier against the real model, raised once per run. */
        private val classifier by lazy { runBlocking { raiseClassifier() } }
    }
}

/** The classifier against the real model, failing when Ollama or its model cannot be reached. */
private suspend fun raiseClassifier(): EntityClassifier {
    val client = KoogEmbeddingsClient()
    client.embed("reachable").toDataOr { error(unreachableMessage) }
    return client.raiseEntityClassifier().toDataOr { error(unreachableMessage) }
}

private const val unreachableMessage = "Ollama with qwen3-embedding:0.6b must be running at localhost:11434"
