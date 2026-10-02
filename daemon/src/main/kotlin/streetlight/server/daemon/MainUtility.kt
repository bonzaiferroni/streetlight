package streetlight.server.daemon

import ai.koog.embeddings.base.Vector
import kampfire.model.Labeled
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.toDataOr
import streetlight.server.daemon.agent.EmbeddingsClient
import streetlight.server.daemon.agent.initializeVectors
import streetlight.server.daemon.agent.toClassifierText
import java.io.File
import kotlin.enums.EnumEntries

/**
 * The vector of each of [entries], read from the embeddings cache when its model and text match, or else embedded
 * with [initializeVectors] and cached, or the first problem.
 */
suspend fun <E> EmbeddingsClient.getCachedVectors(
    entries: EnumEntries<E>,
    getClassifierLabel: (E) -> String,
): Outcome<Map<E, Vector>> where E : Enum<E>, E : Labeled {
    val name = entries.first().declaringJavaClass.simpleName
    val modelFile = embeddingsDir.resolve("$name-model.txt")
    val textFile = embeddingsDir.resolve("$name-text.txt")
    val vectorsFile = embeddingsDir.resolve("$name-vectors.csv")
    val texts = entries.map { it.toClassifierText(getClassifierLabel) }

    if (modelFile.readTextOrNull() == modelId && textFile.readTextOrNull()?.lines() == texts) {
        vectorsFile.readVectors(entries.size)?.let { return Ok(entries.zip(it).toMap()) }
    }

    val vectors = initializeVectors(entries, getClassifierLabel).toDataOr { return it }
    embeddingsDir.mkdirs()
    modelFile.writeText(modelId)
    textFile.writeText(texts.joinToString("\n"))
    vectorsFile.writeText(entries.joinToString("\n") { vectors.getValue(it).values.joinToString(",") })
    return Ok(vectors)
}

/** The [count] vectors this file holds, one per line, or null when it is missing or holds any other number. */
private fun File.readVectors(count: Int): List<Vector>? {
    val lines = readTextOrNull()?.lines()?.takeIf { it.size == count } ?: return null
    return lines.map { line ->
        Vector(line.split(",").map { it.toDoubleOrNull() ?: return null })
    }
}

private fun File.readTextOrNull() = takeIf { it.exists() }?.readText()

private val embeddingsDir = File("../data/embeddings")
