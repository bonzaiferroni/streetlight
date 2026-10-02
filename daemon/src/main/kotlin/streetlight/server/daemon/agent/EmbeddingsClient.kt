package streetlight.server.daemon.agent

import kampfire.model.Problem

/** Embeds text with a language model, for classification by similarity. */
interface EmbeddingsClient

object EmbeddingsProblem {
    val Unspecified = Problem("Unspecified embeddings error.")
}
