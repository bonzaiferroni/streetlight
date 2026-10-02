package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import kampfire.model.Labeled
import kampfire.model.Outcome
import kampfire.model.Problem

/** Embeds text with a language model, for classification by similarity. */
interface EmbeddingsClient {
    /** The id of the model that embeds, which vectors from another model cannot be compared with. */
    val modelId: String

    /** Embeds [text] as a document, bare, to be searched. */
    suspend fun embed(text: String): Outcome<Vector>

    /** Embeds [text] as a query that searches the documents for what [instruction] describes, in its model's format. */
    suspend fun embedQuery(instruction: String, text: String): Outcome<Vector>
}

object EmbeddingsProblem {
    val Unspecified = Problem("Unspecified embeddings error.")
    val NoMatch = Problem("No classification was similar enough.")
}

/** The text this value is embedded from: its label and the description [getClassifierLabel] gives it. */
fun <E> E.toClassifierText(getClassifierLabel: (E) -> String) where E : Enum<E>, E : Labeled =
    "$label: ${getClassifierLabel(this)}"
