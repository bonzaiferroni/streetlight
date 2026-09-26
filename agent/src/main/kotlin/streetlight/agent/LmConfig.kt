package streetlight.agent

import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.ContextWindowStrategy
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.executor.ollama.client.OllamaParams
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import kabinet.utils.Environment
import ai.koog.prompt.params.LLMParams
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The language model behind [KoogHtmlParserClient], and what is needed to reach it.
 *
 * [key] is required by hosted providers, and [baseUrl] overrides the provider's default address.
 * [callInterval] spaces model calls. [think] turns a
 * reasoning model's thinking on or off where the provider supports it.
 */
data class LmConfig(
    val model: LLModel,
    val key: String? = null,
    val baseUrl: String? = null,
    val callInterval: Duration = Duration.ZERO,
    val think: Boolean? = null,
) {
    /** Builds the executor for the provider of [model]. */
    fun toExecutor(): PromptExecutor = when (model.provider) {
        LLMProvider.Google -> MultiLLMPromptExecutor(
            GoogleLLMClient(
                apiKey = requireNotNull(key) { "Google requires a key" },
                httpClientFactory = KtorKoogHttpClient.Factory(),
            )
        )
        LLMProvider.Ollama -> MultiLLMPromptExecutor(
            OllamaClient(
                httpClientFactory = KtorKoogHttpClient.Factory(),
                baseUrl = baseUrl ?: OllamaClient.DEFAULT_BASE_URL,
                contextWindowStrategy = model.contextLength
                    ?.let { ContextWindowStrategy.Companion.Fixed(it) }
                    ?: ContextWindowStrategy.Companion.None,
            )
        )
        else -> error("Unsupported provider: ${model.provider.id}")
    }

    /** The most html characters a prompt can carry within the context length of [model], or `null` when it is unknown. */
    val htmlCharLimit: Int? get() = model.contextLength
        ?.let { ((it - promptReserveTokens) * htmlCharsPerToken).toInt() }

    /** Builds the request params for the provider of [model]. */
    fun toParams(temperature: Double, schema: LLMParams.Schema): LLMParams = when (model.provider) {
        LLMProvider.Ollama -> OllamaParams(temperature = temperature, schema = schema, think = think)
        else -> LLMParams(temperature = temperature, schema = schema)
    }

    companion object {
        /** Gemini 2.5 Flash, spaced to the free tier's rate limit. */
        fun gemini(key: String) = LmConfig(
            model = GoogleModels.Gemini2_5Flash,
            key = key,
            callInterval = 12.seconds,
        )

        /** A local Ollama model named by the tag [id], with thinking turned off. */
        fun ollama(id: String, contextLength: Long, baseUrl: String? = null) = LmConfig(
            model = LLModel(
                provider = LLMProvider.Ollama,
                id = id,
                capabilities = listOf(
                    LLMCapability.Temperature,
                    LLMCapability.Completion,
                    LLMCapability.Schema.JSON.Basic,
                    LLMCapability.Schema.JSON.Standard,
                ),
                contextLength = contextLength,
            ),
            baseUrl = baseUrl,
            think = false,
        )
    }
}

/**
 * Reads the [LmConfig] named by `LM_PROVIDER`, `google` or `ollama`, defaulting to `google`.
 *
 * `google` reads its key from `GEMINI_KEY_A`. `ollama` reads `LM_MODEL`, `LM_CONTEXT_LENGTH` and
 * `LM_BASE_URL`, each with a default.
 */
fun Environment.readLmConfig(): LmConfig = when (val provider = readOrNull("LM_PROVIDER") ?: "google") {
    "google" -> LmConfig.gemini(read("GEMINI_KEY_A"))
    "ollama" -> LmConfig.ollama(
        id = readOrNull("LM_MODEL") ?: "qwen3.5:9b",
        contextLength = readOrNull("LM_CONTEXT_LENGTH")?.toLong() ?: 32768,
        baseUrl = readOrNull("LM_BASE_URL"),
    )
    else -> error("Unknown LM_PROVIDER: $provider")
}

private const val promptReserveTokens = 4096
private const val htmlCharsPerToken = 2.5
