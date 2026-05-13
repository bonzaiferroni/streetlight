import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
//    val trimmer = HtmlTrimmer()
//    val debugFolder = File("debug")
//
//    debugFolder
//        .listFiles()
//        ?.asSequence()
//        ?.filter { it.isFile }
//        ?.filter { it.name.endsWith(".html") }
//        ?.filter { !it.name.endsWith(".reduced.html") }
//        ?.sortedBy { it.name }
//        ?.forEach { file ->
//            val html = file.readText()
//            if (!html.looksLikeHtml()) return@forEach
//            val reduced = trimmer.trimHtml(html)
//            val percentReduced = (100.0 * (html.length - reduced.length) / html.length).toInt()
//            val report = "from ${html.length} to ${reduced.length} ($percentReduced%)"
//            println("${report.padEnd(36)} ${file.name}")
//
//            val output = File(file.parentFile, "${file.nameWithoutExtension}.reduced.html")
//            output.writeText(reduced)
//        }
}

private val htmlStart = Regex("""^\s*(<!DOCTYPE\s+html|<html|<[a-zA-Z]+)""", RegexOption.IGNORE_CASE)

fun String.looksLikeHtml(): Boolean =
    htmlStart.containsMatchIn(this)

//    val agent = ChatAgentConnection(apiKey)
//    agent.connect()

//    val result = agent.read("https://www.reddit.com/r/AuroraCO/comments/1r33pww/dog_runaway_from_petco_mississippi_and_havana_area/")
//    println(result)

//    val env = readEnvFromPath()
//    val apiKey = env.read("GEMINI_KEY_A")
//
//    val agent = UrlParser(apiKey)
//    val result: ColdParse? =
//        agent.readImage("../upload/c285c6c0-0b15-4113-a1e9-0add48984ac9.jpg", eventInstructions)
//
//     println(result)