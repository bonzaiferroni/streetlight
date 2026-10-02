package streetlight.server.daemon.agent

/** The crawler's room for classifying what it reads, by embeddings from [client]. */
class EntityClassifier(
    private val client: EmbeddingsClient,
)
