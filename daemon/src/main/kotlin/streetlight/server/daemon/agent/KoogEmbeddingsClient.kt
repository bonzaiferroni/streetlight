package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import ai.koog.embeddings.local.LLMEmbedder
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kampfire.model.Ok
import kampfire.model.Outcome
import klutch.utils.logger
import kotlinx.coroutines.CancellationException

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
    private val log = KotlinLogging.logger(KoogEmbeddingsClient::class)

    override val modelId get() = model.id

    override suspend fun embed(text: String): Outcome<Vector> = try {
        Ok(embedder.embed(text))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        log.error(e) { "Embedding failed: ${text.take(50)}" }
        EmbeddingsProblem.Unspecified
    }

    override suspend fun embedQuery(instruction: String, text: String): Outcome<Vector> =
        embed("Instruct: $instruction\nQuery:$text")
}

private val qwenEmbeddingModel = LLModel(
    provider = LLMProvider.Ollama,
    id = "qwen3-embedding:0.6b",
    capabilities = listOf(LLMCapability.Embed),
    contextLength = 32_768,
)
