package streetlight.server.plugins

import io.ktor.server.application.*
import klutch.environment.readEnvFromPath
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import streetlight.server.db.tables.*

fun Application.configureDatabases() {
    initDb(env)
}

val env = readEnvFromPath()
