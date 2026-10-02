package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import kampfire.model.Labeled
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.toDataOr
import kotlin.enums.EnumEntries

/** Embeds text with a language model, for classification by similarity. */
interface EmbeddingsClient {
    /** The id of the model that embeds, which vectors from another model cannot be compared with. */
    val modelId: String

    suspend fun embed(text: String): Outcome<Vector>
}

object EmbeddingsProblem {
    val Unspecified = Problem("Unspecified embeddings error.")
}

/**
 * The vector of each of [entries], embedded from its label and the description [getClassifierLabel] gives it, or the
 * first problem.
 */
suspend fun <E> EmbeddingsClient.initializeVectors(
    entries: EnumEntries<E>,
    getClassifierLabel: (E) -> String,
): Outcome<Map<E, Vector>> where E : Enum<E>, E : Labeled {
    val vectors = entries.associateWith { entry ->
        embed(entry.toClassifierText(getClassifierLabel)).toDataOr { return it }
    }
    return Ok(vectors)
}

/** The text this value is embedded from: its label and the description [getClassifierLabel] gives it. */
fun <E> E.toClassifierText(getClassifierLabel: (E) -> String) where E : Enum<E>, E : Labeled =
    "$label: ${getClassifierLabel(this)}"
