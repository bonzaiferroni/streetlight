package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Outcome
import kampfire.model.Url
import kampfire.model.toDataOr
import kotlinx.coroutines.delay
import streetlight.agent.FetchText
import streetlight.agent.StreetlightAgent
import streetlight.agent.parseHtmlDocument
import streetlight.model.data.FetchMode
import streetlight.model.data.Origin
import streetlight.model.data.toOriginId
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class RobotGate(
    text: String?,
    private val agent: String = "*",
    defaultDelay: Duration = 10.seconds,
) {

    private data class Rule(val pattern: String, val allow: Boolean)

    private val rules: List<Rule>
    val interval: Duration
    var lastRead: Instant? = null
        private set

    init {
        val groups = mutableMapOf<String, MutableList<Rule>>()
        val delays = mutableMapOf<String, Duration>()
        var current = mutableSetOf<String>()
        var expectingAgents = false

        val lines = text?.lineSequence() ?: emptySequence()
        for (raw in lines) {
            val line = raw.substringBefore('#').trim()
            if (line.isEmpty()) continue
            val field = line.substringBefore(':').trim().lowercase()
            val value = line.substringAfter(':', "").trim()

            when (field) {
                "user-agent" -> {
                    if (!expectingAgents) current = mutableSetOf()
                    expectingAgents = true
                    current.add(value.lowercase())
                }
                "disallow", "allow" -> {
                    expectingAgents = false
                    if (value.isEmpty() && field == "disallow") continue
                    if (value.isEmpty()) continue
                    val rule = Rule(value, field == "allow")
                    current.forEach { groups.getOrPut(it) { mutableListOf() }.add(rule) }
                }
                "crawl-delay" -> {
                    expectingAgents = false
                    value.toDoubleOrNull()?.let { seconds ->
                        current.forEach { delays[it] = seconds.seconds }
                    }
                }
            }
        }

        val key = groups.keys.firstOrNull { it == agent.lowercase() } ?: "*"
        rules = groups[key].orEmpty()
        interval = delays[key] ?: defaultDelay
    }

    suspend fun fetchWhenOpen(url: Url, mode: FetchMode): Outcome<FetchText> {
        waitUntilOpen(url)
        return fetchText(url, mode)
    }

    suspend fun waitUntilOpen(url: Url) = waitUntilOpen(url.toRelativePath())

    suspend fun waitUntilOpen(url: String): Boolean {
        if (!isOpen(url)) return false

        lastRead?.let { last ->
            val remaining = interval - (Clock.System.now() - last)
            if (remaining.isPositive()) delay(remaining)
        }
        lastRead = Clock.System.now()
        return true
    }

    fun isOpen(url: String): Boolean {
        val path = url.ifEmpty { "/" }
        val match = rules
            .filter { matches(it.pattern, path) }
            .maxWithOrNull(compareBy({ it.pattern.trimEnd('$').length }, { it.allow }))
        return match?.allow ?: true
    }

    private fun matches(pattern: String, path: String): Boolean {
        val anchored = pattern.endsWith('$')
        val body = if (anchored) pattern.dropLast(1) else pattern
        val segments = body.split('*')

        var cursor = 0
        segments.forEachIndexed { index, segment ->
            if (index == 0) {
                if (!path.startsWith(segment)) return false
                cursor = segment.length
            } else {
                val found = path.indexOf(segment, cursor)
                if (found < 0) return false
                cursor = found + segment.length
            }
        }
        return !anchored || cursor == path.length
    }
}

fun String?.toRobotGate() = RobotGate(this, StreetlightAgent.AgentToken)

