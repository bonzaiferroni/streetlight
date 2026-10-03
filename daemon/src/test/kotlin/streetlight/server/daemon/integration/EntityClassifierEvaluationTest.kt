package streetlight.server.daemon.integration

import kampfire.model.toDataOr
import kotlinx.coroutines.runBlocking
import streetlight.model.data.EventTag
import streetlight.server.daemon.agent.EntityClassifier
import streetlight.server.daemon.agent.KoogEmbeddingsClient
import streetlight.server.daemon.agent.eventInstruction
import streetlight.server.daemon.agent.getEmbeddingsText
import streetlight.server.daemon.agent.readEventEdits
import streetlight.server.daemon.getCachedVectors
import streetlight.server.daemon.raiseEntityClassifier
import kotlin.test.Test
import kotlin.test.assertTrue

class EntityClassifierEvaluationTest {

    @Test
    fun `held-out events are tagged with the precision of the current example set`() = runBlocking {
        val events = readEventEdits("event-evaluation.json")
        val texts = events.map { it.copy(tags = emptyList()).getEmbeddingsText() }
        val vectors = client.getCachedVectors("EventEvaluation", texts, eventInstruction)
            .toDataOr { error(unreachableMessage) }

        val givenTags = vectors.map { classifier.readTags(it).tags }
        val scores = EventTag.entries.mapNotNull { tag ->
            val expected = events.map { tag in it.tags }
            val given = givenTags.map { tag in it }
            if (expected.none { it }) return@mapNotNull null
            TagScore(tag, expected, given)
        }
        scores.sortedBy { it.f05 }.forEach { println(it) }
        val macroF05 = scores.map { it.f05 }.average()
        println("macro F0.5 = ${"%.3f".format(macroF05)} over ${scores.size} tags")

        assertTrue(macroF05 >= minMacroF05, "macro F0.5 should be at least $minMacroF05, was $macroF05")
    }

    companion object {
        private val client by lazy {
            KoogEmbeddingsClient().also { client ->
                runBlocking { client.embed("reachable").toDataOr { error(unreachableMessage) } }
            }
        }

        /** The classifier against the real model, raised once per run. */
        private val classifier: EntityClassifier by lazy {
            runBlocking { client.raiseEntityClassifier().toDataOr { error(unreachableMessage) } }
        }
    }
}

/** The precision and recall of the [given] tag against the [expected] one, across the evaluation set. */
private class TagScore(val tag: EventTag, expected: List<Boolean>, given: List<Boolean>) {
    private val count = expected.count { it }
    private val truePositives = expected.zip(given).count { (e, g) -> e && g }
    private val precision = given.count { it }.let { if (it == 0) 0.0 else truePositives.toDouble() / it }
    private val recall = truePositives.toDouble() / count

    /** The F-score weighting precision twice as heavily as recall. */
    val f05 = if (precision + recall == 0.0) 0.0 else 1.25 * precision * recall / (0.25 * precision + recall)

    override fun toString() =
        "${tag.name.padEnd(18)} n=${count.toString().padStart(3)} P=${"%.2f".format(precision)} " +
            "R=${"%.2f".format(recall)} F0.5=${"%.2f".format(f05)}"
}

private const val minMacroF05 = 0.715
private const val unreachableMessage = "Ollama with qwen3-embedding:0.6b must be running at localhost:11434"
