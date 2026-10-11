package streetlight.server.daemon.integration

import kampfire.model.toDataOr
import kotlinx.coroutines.runBlocking
import streetlight.model.data.EventTag
import streetlight.model.data.LocationTag
import streetlight.server.daemon.agent.EntityClassifier
import streetlight.server.daemon.agent.KoogEmbeddingsClient
import streetlight.server.daemon.agent.eventInstruction
import streetlight.server.daemon.agent.getEmbeddingsText
import streetlight.server.daemon.agent.locationInstruction
import streetlight.server.daemon.agent.readEventEdits
import streetlight.server.daemon.agent.readLocationEdits
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

        val givenTags = vectors.map { classifier.readEventTags(it).tags }
        val macroF05 = scoreTags(EventTag.entries, events.map { it.tags }, givenTags)

        assertTrue(macroF05 >= minEventMacroF05, "macro F0.5 should be at least $minEventMacroF05, was $macroF05")
    }

    @Test
    fun `held-out locations are tagged with the precision of the current example set`() = runBlocking {
        val locations = readLocationEdits("location-evaluation.json")
        val texts = locations.map { it.copy(tags = emptyList()).getEmbeddingsText() }
        val vectors = client.getCachedVectors("LocationEvaluation", texts, locationInstruction)
            .toDataOr { error(unreachableMessage) }

        val givenTags = vectors.map { classifier.readLocationTags(it).tags }
        val macroF05 = scoreTags(LocationTag.entries, locations.map { it.tags }, givenTags)

        assertTrue(macroF05 >= minLocationMacroF05, "macro F0.5 should be at least $minLocationMacroF05, was $macroF05")
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

/**
 * The macro F0.5 of the [givenTags] against the [expectedTags] over each of [tags] the evaluation set carries, each
 * tag's score printed, the lowest first.
 */
private fun <T: Enum<T>> scoreTags(tags: List<T>, expectedTags: List<List<T>>, givenTags: List<List<T>>): Double {
    val scores = tags.mapNotNull { tag ->
        val expected = expectedTags.map { tag in it }
        val given = givenTags.map { tag in it }
        if (expected.none { it }) return@mapNotNull null
        TagScore(tag.name, expected, given)
    }
    scores.sortedBy { it.f05 }.forEach { println(it) }
    val macroF05 = scores.map { it.f05 }.average()
    println("macro F0.5 = ${"%.3f".format(macroF05)} over ${scores.size} tags")
    return macroF05
}

/** The precision and recall of the tag [name], [given] against [expected], across the evaluation set. */
private class TagScore(val name: String, expected: List<Boolean>, given: List<Boolean>) {
    private val count = expected.count { it }
    private val truePositives = expected.zip(given).count { (e, g) -> e && g }
    private val precision = given.count { it }.let { if (it == 0) 0.0 else truePositives.toDouble() / it }
    private val recall = truePositives.toDouble() / count

    /** The F-score weighting precision twice as heavily as recall. */
    val f05 = if (precision + recall == 0.0) 0.0 else 1.25 * precision * recall / (0.25 * precision + recall)

    override fun toString() =
        "${name.padEnd(18)} n=${count.toString().padStart(3)} P=${"%.2f".format(precision)} " +
            "R=${"%.2f".format(recall)} F0.5=${"%.2f".format(f05)}"
}

private const val minEventMacroF05 = 0.715
private const val minLocationMacroF05 = 0.475
private const val unreachableMessage = "Ollama with qwen3-embedding:0.6b must be running at localhost:11434"
