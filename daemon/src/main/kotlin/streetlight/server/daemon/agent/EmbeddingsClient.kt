package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
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
}
