package streetlight.agent

import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.params.LLMParams
import com.fleeksoft.ksoup.nodes.Document
import io.github.oshai.kotlinlogging.KotlinLogging
import kabinet.utils.Environment
import kampfire.model.Outcome
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.utils.takeEllipsis
import klutch.utils.logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.io.files.Path
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class KoogHtmlParserClient(
    env: Environment,
    private val retryDelay: Duration = 30.seconds,
    private val callInterval: Duration = 12.seconds,
): HtmlParserClient {
    private val callMutex = Mutex()
    private var lastCallAt = Instant.DISTANT_PAST
    private val executor = simpleGoogleAIExecutor(env.read("GEMINI_KEY_A"))
    private val console = KotlinLogging.logger("dao")
    private val cache = mutableMapOf<Int, ParserContent>()
    private val trimmer = HtmlTrimmer()
    private val log = KotlinLogging.logger(KoogHtmlParserClient::class)

    override suspend fun <T: Any> readHtml(url: Url, doc: Document, instructions: String, type: KClass<T>): Outcome<T>
        = readHtml(url, doc, instructions, type.createType())

    override suspend fun <T: Any> readHtml(
        url: Url,
        doc: Document,
        instructions: String,
        type: KType,
        retryCount: Int,
    ): Outcome<T> {
        val response = withCache(doc.hashCode()) {
            readHtmlContent(url, doc, instructions, type, retryCount)
        }
        return when (response) {
            is Ok -> tryDecode<T>(response.data.json, type)?.let { Ok(it) } ?: LMProblem.Decoding
            is Problem -> Problem(response.message)
        }
    }

    private suspend fun withCache(cacheKey: Int, block: suspend () -> Outcome<ParserContent>): Outcome<ParserContent> {
        val cachedContent = cache[cacheKey]
        if (cachedContent != null) return Ok(cachedContent)

        val response = block()
        if (response is Ok) {
            cache[cacheKey] = response.data

            if (cache.size > 25) {
                val firstKey = cache.keys.firstOrNull()
                firstKey?.let { key -> cache.remove(key) }
            }
        }

        return response
    }

    private suspend fun readHtmlContent(
        url: Url,
        doc: Document,
        instructions: String,
        type: KType,
        retryCount: Int,
    ): Outcome<ParserContent> {
        log.info { "Reading html: ${url.value.take(50)}" }
        val content = trimmer.trimHtml(doc)

        val prompt = prompt(
            id = "dev-assistant",
            params = LLMParams(
                temperature = 0.5,
                schema = type.toBasicSchema()
            )
        ) {
            system("You read web pages and extract relevant information as json.")

            user("$instructions\n\nFor reference, here is the url:\n$url\n\nHere is the HTML:\n$content")
        }

        logger.info { "LM Parse: $url" }

        return when (val outcome = executePrompt(prompt, retryCount)) {
            is Problem -> outcome
            is Ok -> Ok(ParserContent(document = doc, json = outcome.data))
        }
    }

    private fun LLMClientException.isBusy(): Boolean =
        toString().contains("UNAVAILABLE") || toString().contains("503") || toString().contains("429")

    /** The delay from the `RetryInfo` detail of a Google error body, or `null`. */
    private fun LLMClientException.readRetryDelay(): Duration? = runCatching {
        val text = toString()
        val body = text.substring(text.indexOf('{'), text.lastIndexOf('}') + 1)
        Json.parseToJsonElement(body).jsonObject["error"]!!.jsonObject["details"]!!.jsonArray
            .map { it.jsonObject }
            .first { it["@type"]?.jsonPrimitive?.content?.endsWith("RetryInfo") == true }
            .getValue("retryDelay").jsonPrimitive.content
            .let { Duration.parse(it) }
    }.getOrNull()

    private suspend fun executePrompt(prompt: Prompt, retryCount: Int): Outcome<String> {
        repeat(retryCount) { attempt ->
            try {
                waitForCallInterval()
                return Ok(executor.execute(prompt, GoogleModels.Gemini2_5Flash).first().content)
            } catch (e: LLMClientException) {
                log.error { e }
                if (!e.isBusy()) return LMProblem.Unspecified
                if (attempt == retryCount - 1) return LMProblem.Busy
                val wait = e.readRetryDelay() ?: retryDelay
                log.info { "Model busy, retrying in ${wait.inWholeSeconds}s (attempt ${attempt + 1} of $retryCount)" }
                delay(wait)
            }
        }
        return LMProblem.Unspecified
    }

    private suspend fun waitForCallInterval() = callMutex.withLock {
        val remaining = callInterval - (Clock.System.now() - lastCallAt)
        if (remaining.isPositive()) delay(remaining)
        lastCallAt = Clock.System.now()
    }

    override suspend fun <T> readImage(url: String, instructions: String, type: KType): T? {
        val cacheKey = url.hashCode()
        val cached = cache[cacheKey]
        if (cached != null) return tryDecode(cached.json, type)

        val prompt = prompt(
            id = "dev-assistant",
            params = LLMParams(
                temperature = 0.5,
                schema = type.toBasicSchema()
            )
        ) {
            system("You read images and extract relevant information as json.")

            user {
                +instructions

//                image(
//                    ContentPart.Image(
//                        content = AttachmentContent.URL(url),
//                        format = "jpg",
//                        mimeType = "image/jpg",
//                        fileName = "c285c6c0-0b15-4113-a1e9-0add48984ac9.jpg"
//                    )
//                )
                image(Path(url))
            }
        }

        val json = executor.execute(prompt, GoogleModels.Gemini2_5Flash).first().content
        cache[cacheKey] = ParserContent(
            document = null,
            json = json
        )

        if (cache.size > 10) {
            val firstKey = cache.keys.firstOrNull()
            firstKey?.let { cache.remove(it) }
        }
        return tryDecode(json, type)
    }

    private fun <T> tryDecode(text: String, type: KType): T? = try {
        decodeLenient(text, type)
    } catch (e: Exception) {
        console.error { e }
        console.error { "unable to decode structured llm response:\n${text.takeEllipsis(400)}" }
        null
    }
}

data class ParserContent(
    val document: Document?,
    val json: String,
)

private val logger = KotlinLogging.logger(KoogHtmlParserClient::class)

object LMProblem {
    val Busy = Problem("Language model is busy.")
    val Unspecified = Problem("Unspecified language model error.")
    val Decoding = Problem("Unable to decode LM response.")
    val UsageLimit = Problem("LM has reached its usage limit.")
}