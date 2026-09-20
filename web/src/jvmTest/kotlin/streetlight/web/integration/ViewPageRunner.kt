package streetlight.web.integration

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.TimeoutError
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

class ViewPageRunner(
    private val browser: Browser,
    private val distDir: Path,
    private val timeoutSeconds: Long = 60,
) : AutoCloseable {

    private val server = HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
        createContext("/") { exchange ->
            val name = exchange.requestURI.path.trimStart('/').ifEmpty { TEST_PAGE }
            val file = distDir.resolve(name).normalize()
            if (!file.startsWith(distDir) || !Files.isRegularFile(file)) {
                exchange.sendResponseHeaders(404, -1)
            } else {
                val bytes = Files.readAllBytes(file).let {
                    if (name == TEST_PAGE) String(it).replace("unpkg.com/mocha/", "unpkg.com/mocha@$MOCHA_VERSION/").toByteArray() else it
                }
                exchange.responseHeaders.add("Content-Type", contentTypeOf(name))
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            exchange.close()
        }
        start()
    }

    fun run(include: String): PageRun =
        load("""{"kotlinTestCliArguments":["--include",${include.toJsonString()}]}""")

    fun discover(): List<String> =
        load("""{"mochaSetupOptions":{"dryRun":true}}""").testNames

    private fun load(config: String): PageRun {
        val url = "http://localhost:${server.address.port}/$TEST_PAGE?kotlinTestConfig=${URLEncoder.encode(config, "UTF-8")}"

        browser.newContext().use { context ->
            val page = context.newPage()
            val lines = mutableListOf<String>()
            val finished = CompletableFuture<Unit>()
            page.onConsoleMessage { message ->
                val text = message.text()
                lines += text
                if (text == FINISHED_MARKER) finished.complete(Unit)
            }
            val problems = mutableListOf<String>()
            page.onPageError { problems += "page error: $it" }
            page.onRequestFailed { problems += "request failed: ${it.url()} ${it.failure()}" }
            page.onResponse { if (it.status() >= 400) problems += "response ${it.status()}: ${it.url()}" }
            page.navigate(url)
            try {
                page.waitForCondition(
                    { finished.isDone },
                    Page.WaitForConditionOptions().setTimeout(timeoutSeconds * 1000.0),
                )
            } catch (e: TimeoutError) {
                error(
                    "the page did not finish within ${timeoutSeconds}s\n" +
                        "problems:\n${problems.joinToString("\n")}\n" +
                        "console output:\n${lines.joinToString("\n")}"
                )
            }
            return PageRun(lines)
        }
    }

    override fun close() = server.stop(0)
}

class PageRun(val lines: List<String>) {
    val started = lines.teamcity("testStarted")
    val testNames: List<String> = run {
        val suites = ArrayDeque<String>()
        buildList {
            lines.forEach { line ->
                val name = Regex("""^##teamcity\[(\w+) name='((?:[^'|]|\|.)*)'""").find(line) ?: return@forEach
                val text = name.groupValues[2].unescapeTeamcity()
                when (name.groupValues[1]) {
                    "testSuiteStarted" -> suites.addLast(text)
                    "testSuiteFinished" -> suites.removeLast()
                    "testStarted" -> add((suites + text).joinToString("."))
                }
            }
        }
    }
    val failures = lines.teamcity("testFailed").map { it.attributes["message"].orEmpty() + "\n" + it.attributes["details"].orEmpty() }
    val output = lines.filterNot { it.startsWith("##teamcity[") || it == FINISHED_MARKER }
}

class TeamcityMessage(val attributes: Map<String, String>)

private fun List<String>.teamcity(kind: String): List<TeamcityMessage> = mapNotNull { line ->
    if (!line.startsWith("##teamcity[$kind ")) return@mapNotNull null
    TeamcityMessage(
        Regex("""(\w+)='((?:[^'|]|\|.)*)'""").findAll(line).associate { it.groupValues[1] to it.groupValues[2].unescapeTeamcity() }
    )
}

private fun String.unescapeTeamcity() = Regex("""\|(.)""").replace(this) {
    when (val c = it.groupValues[1]) {
        "n" -> "\n"
        "r" -> "\r"
        else -> c
    }
}

private fun String.toJsonString() = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

private fun contentTypeOf(name: String) = when {
    name.endsWith(".html") -> "text/html"
    name.endsWith(".js") -> "text/javascript"
    name.endsWith(".map") || name.endsWith(".json") -> "application/json"
    else -> "application/octet-stream"
}

private const val TEST_PAGE = "test.html"
private const val FINISHED_MARKER = "KOTLIN-TEST-FINISHED"
private const val MOCHA_VERSION = "10.8.2"
