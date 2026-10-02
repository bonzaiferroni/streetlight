package streetlight.server.daemon.agent

import ai.koog.embeddings.local.LLMEmbedder
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

/** An [EmbeddingsClient] that embeds with the Ollama [model] served at [baseUrl]. */
class KoogEmbeddingsClient(
    private val model: LLModel = qwenEmbeddingModel,
    private val baseUrl: String = OllamaClient.DEFAULT_BASE_URL,
): EmbeddingsClient {
    private val client = OllamaClient(
        httpClientFactory = KtorKoogHttpClient.Factory(),
        baseUrl = baseUrl,
    )
    private val embedder = LLMEmbedder(client, model)
}

private val qwenEmbeddingModel = LLModel(
    provider = LLMProvider.Ollama,
    id = "qwen3-embedding:0.6b",
    capabilities = listOf(LLMCapability.Embed),
    contextLength = 32_768,
)
