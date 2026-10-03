package streetlight.server.daemon

import ai.koog.embeddings.base.Vector
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.toDataOr
import streetlight.server.daemon.agent.EmbeddingsClient
import streetlight.server.daemon.agent.EntityClassifier
import streetlight.server.daemon.agent.eventInstruction
import streetlight.server.daemon.agent.getEmbeddingsText
import streetlight.server.daemon.agent.readEventEdits
import streetlight.server.daemon.agent.toFeatures
import streetlight.server.daemon.agent.trainTagModels
import java.io.File

/**
 * The [EntityClassifier] deciding each tag by a regression trained on the examples, their vectors centered on the
 * examples' average, or the first problem.
 */
suspend fun EmbeddingsClient.raiseEntityClassifier(): Outcome<EntityClassifier> {
    val examples = readEventEdits("event-examples.json")
    val exampleTexts = examples.map { it.getEmbeddingsText() }
    val exampleVectors = getCachedVectors("EventExamples", exampleTexts, eventInstruction).toDataOr { return it }

    val center = exampleVectors.toCentroid().values.toDoubleArray()
    val tagModels = trainTagModels(exampleVectors.map { it.toFeatures(center) }, examples.map { it.tags })

    return Ok(EntityClassifier(this, center, tagModels))
}

/**
 * The vector of each of [texts], read from the embeddings cache [name] when its model, [instruction] and texts match,
 * or else embedded and cached, or the first problem. A text is embedded as a query when [instruction] is given, and as
 * a document otherwise.
 */
suspend fun EmbeddingsClient.getCachedVectors(
    name: String,
    texts: List<String>,
    instruction: String? = null,
): Outcome<List<Vector>> {
    val modelFile = embeddingsDir.resolve("$name-model.txt")
    val textFile = embeddingsDir.resolve("$name-text.txt")
    val vectorsFile = embeddingsDir.resolve("$name-vectors.csv")
    val model = listOfNotNull(modelId, instruction).joinToString("\n")
    val textLines = texts.map { it.replace("\\", "\\\\").replace("\n", "\\n") }

    if (modelFile.readTextOrNull() == model && textFile.readTextOrNull()?.lines() == textLines) {
        vectorsFile.readVectors(texts.size)?.let { return Ok(it) }
    }

    val vectors = texts.map { text ->
        val vector = instruction?.let { embedQuery(it, text) } ?: embed(text)
        vector.toDataOr { return it }
    }
    embeddingsDir.mkdirs()
    modelFile.writeText(model)
    textFile.writeText(textLines.joinToString("\n"))
    vectorsFile.writeText(vectors.joinToString("\n") { it.values.joinToString(",") })
    return Ok(vectors)
}

/** The average of these vectors. */
private fun List<Vector>.toCentroid() = Vector(first().values.indices.map { i -> sumOf { it.values[i] } / size })

/** The [count] vectors this file holds, one per line, or null when it is missing or holds any other number. */
private fun File.readVectors(count: Int): List<Vector>? {
    val lines = readTextOrNull()?.lines()?.takeIf { it.size == count } ?: return null
    return lines.map { line ->
        Vector(line.split(",").map { it.toDoubleOrNull() ?: return null })
    }
}

private fun File.readTextOrNull() = takeIf { it.exists() }?.readText()

private val embeddingsDir = File("../data/embeddings")
