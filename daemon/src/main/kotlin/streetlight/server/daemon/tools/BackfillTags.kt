package streetlight.server.daemon.tools

import kabinet.utils.Environment
import kampfire.model.toDataOr
import klutch.server.KoinProvider
import klutch.server.provide
import kotlinx.coroutines.runBlocking
import org.koin.dsl.koinApplication
import streetlight.model.data.EventEdit
import streetlight.server.daemon.agent.KoogEmbeddingsClient
import streetlight.server.daemon.crawler.withoutNote
import streetlight.server.daemon.raiseEntityClassifier
import streetlight.server.db.connectDb
import streetlight.server.model.DaoFacade
import streetlight.server.model.serverModule

/**
 * Gives tags to every stored event without any, classified from its title, its description without the note the
 * crawler added, its website and its minimum age.
 */
fun main() = runBlocking {
    val provider = KoinProvider(koinApplication { modules(serverModule) }.koin)
    val dao = provider.provide<DaoFacade>()
    connectDb(provider.provide<Environment>())
    val classifier = KoogEmbeddingsClient().raiseEntityClassifier()
        .toDataOr { error("Unable to raise the classifier: ${it.message}") }

    val events = dao.event.readUntaggedEvents()
    var tagged = 0
    var untagged = 0
    var failed = 0
    events.forEach { event ->
        val edit = EventEdit(
            title = event.title,
            description = event.description?.withoutNote(),
            website = event.website,
            ageMin = event.ageMin,
        )
        var isClassified = false
        val classifiedEdit = classifier.classifyEvent(edit) { isClassified = true }
        when {
            !isClassified -> failed++
            classifiedEdit.tags.isEmpty() -> untagged++
            else -> {
                dao.event.updateTags(event.eventId, classifiedEdit.tags)
                tagged++
            }
        }
    }

    println("${events.size} untagged events: $tagged tagged, $untagged given no tag, $failed failed to embed")
}
