package streetlight.server.daemon

import ai.koog.embeddings.base.Vector
import kampfire.model.Labeled
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.toDataOr
import streetlight.model.data.EventSubtype
import streetlight.model.data.EventType
import streetlight.server.daemon.agent.EmbeddingsClient
import streetlight.server.daemon.agent.EntityClassifier
import streetlight.server.daemon.agent.eventInstruction
import streetlight.server.daemon.agent.getClassifierLabel
import streetlight.server.daemon.agent.getEmbeddingsText
import streetlight.server.daemon.agent.readClassifiedEvents
import streetlight.server.daemon.agent.toClassifierText
import java.io.File
import kotlin.enums.EnumEntries

/**
 * The [EntityClassifier] comparing events against the centroid of each value's examples, or the value's label where it
 * has none, or the first problem.
 */
suspend fun EmbeddingsClient.raiseEntityClassifier(): Outcome<EntityClassifier> {
    val typeLabelVectors = getCachedVectors(EventType.entries) { it.getClassifierLabel() }.toDataOr { return it }
    val subtypeLabelVectors = getCachedVectors(EventSubtype.entries) { it.getClassifierLabel() }.toDataOr { return it }
    val examples = readClassifiedEvents("event-examples.json")
    val exampleTexts = examples.map { it.event.getEmbeddingsText() }
    val exampleVectors = getCachedVectors("EventExamples", exampleTexts, eventInstruction).toDataOr { return it }

    val classifiedVectors = examples.zip(exampleVectors)
    val typeCentroids = classifiedVectors
        .groupBy({ (example, _) -> example.eventType }, { (_, vector) -> vector })
        .mapValues { (_, vectors) -> vectors.toCentroid() }
    val subtypeCentroids = classifiedVectors
        .mapNotNull { (example, vector) -> example.eventSubtype?.let { it to vector } }
        .groupBy({ (eventSubtype, _) -> eventSubtype }, { (_, vector) -> vector })
        .mapValues { (_, vectors) -> vectors.toCentroid() }

    return Ok(EntityClassifier(this, typeLabelVectors + typeCentroids, subtypeLabelVectors + subtypeCentroids))
}

/** The vector of each of [entries], embedded from its label and the description [getClassifierLabel] gives it. */
suspend fun <E> EmbeddingsClient.getCachedVectors(
    entries: EnumEntries<E>,
    getClassifierLabel: (E) -> String,
): Outcome<Map<E, Vector>> where E : Enum<E>, E : Labeled {
    val name = entries.first().declaringJavaClass.simpleName
    val texts = entries.map { it.toClassifierText(getClassifierLabel) }
    val vectors = getCachedVectors(name, texts).toDataOr { return it }
    return Ok(entries.zip(vectors).toMap())
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
