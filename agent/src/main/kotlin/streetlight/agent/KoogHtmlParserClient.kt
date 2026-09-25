package streetlight.agent

import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClientException
import com.fleeksoft.ksoup.nodes.Document
import io.github.oshai.kotlinlogging.KotlinLogging
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
import kotlin.time.TimeSource

class KoogHtmlParserClient(
    private val config: LmConfig,
    private val retryDelay: Duration = 30.seconds,
): HtmlParserClient {
    private val callMutex = Mutex()
    private var lastCallAt = Instant.DISTANT_PAST
    private val executor = config.toExecutor()
    private val console = KotlinLogging.logger("dao")
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
        observer: HtmlParseObserver?,
    ): Outcome<T> {
        return when (val response = readHtmlContent(url, doc, instructions, type, retryCount, observer)) {
            is Ok -> tryDecode<T>(response.data, type)?.let { Ok(it) } ?: LMProblem.Decoding
            is Problem -> response
        }
    }

    private suspend fun readHtmlContent(
        url: Url,
        doc: Document,
        instructions: String,
        type: KType,
        retryCount: Int,
        observer: HtmlParseObserver?,
    ): Outcome<String> {
        log.info { "Reading html: ${url.value.take(50)}" }
        val trimmed = trimmer.trimHtml(doc)
        val content = config.htmlCharLimit?.let { limit ->
            if (trimmed.html.length > limit) log.warn { "Truncating html from ${trimmed.html.length} to $limit chars: $url" }
            trimmed.html.take(limit)
        } ?: trimmed.html
        observer?.trimmed(trimmed, content)

        val prompt = prompt(
            id = "dev-assistant",
            params = config.toParams(temperature = lmTemperature, schema = type.toStandardSchema())
        ) {
            system("You read web pages and respond with json, following the instructions that come after the page.")

            user("Here is the HTML of $url:\n$content\n\n$instructions")
        }

        logger.info { "LM Parse: $url" }

        return executePrompt(prompt, retryCount, observer)
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

    private suspend fun executePrompt(prompt: Prompt, retryCount: Int, observer: HtmlParseObserver?): Outcome<String> {
        repeat(retryCount) { attempt ->
            try {
                waitForCallInterval()
                val start = TimeSource.Monotonic.markNow()
                val response = executor.execute(prompt, config.model)
                val json = response.textContent()
                observer?.responded(
                    model = config.model.id,
                    json = json,
                    inputTokens = response.metaInfo.inputTokensCount,
                    outputTokens = response.metaInfo.outputTokensCount,
                    millis = start.elapsedNow().inWholeMilliseconds,
                    attempts = attempt + 1,
                )
                return Ok(json)
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
        val remaining = config.callInterval - (Clock.System.now() - lastCallAt)
        if (remaining.isPositive()) delay(remaining)
        lastCallAt = Clock.System.now()
    }

    override suspend fun <T> readImage(url: String, instructions: String, type: KType): T? {
        val prompt = prompt(
            id = "dev-assistant",
            params = config.toParams(temperature = lmTemperature, schema = type.toStandardSchema())
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

        val json = executor.execute(prompt, config.model).textContent()
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

private val logger = KotlinLogging.logger(KoogHtmlParserClient::class)

object LMProblem {
    val Busy = Problem("Language model is busy.")
    val Unspecified = Problem("Unspecified language model error.")
    val Decoding = Problem("Unable to decode LM response.")
    val UsageLimit = Problem("LM has reached its usage limit.")
}

private const val lmTemperature = 0.1
