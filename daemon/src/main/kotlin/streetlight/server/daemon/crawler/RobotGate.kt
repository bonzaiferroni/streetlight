package streetlight.server.daemon.crawler

import kampfire.model.Problem
import kampfire.model.Url
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import streetlight.server.model.StreetlightAgent
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

object RobotProblem {
    val Disallowed = Problem("Path is disallowed by robots.txt.")
}

class RobotGate(
    text: String?,
    private val agent: String = "*",
    defaultDelay: Duration = 10.seconds,
) {

    private data class Rule(val pattern: String, val allow: Boolean)

    private val rules: List<Rule>
    val interval: Duration
    private val slotMutex = Mutex()
    private var nextOpen = Instant.DISTANT_PAST

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

    suspend fun waitUntilOpen(url: Url) = waitUntilOpen(url.toRelativePath())

    /**
     * Whether [url] is open to the crawler, waiting first for its turn: each caller reserves the next slot, one
     * [interval] after the last, so callers on the same origin are spaced even when they arrive together.
     */
    suspend fun waitUntilOpen(url: String): Boolean {
        if (!isOpen(url)) return false
        val slot = slotMutex.withLock {
            maxOf(Clock.System.now(), nextOpen).also { nextOpen = it + interval }
        }
        delay(slot - Clock.System.now())
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

