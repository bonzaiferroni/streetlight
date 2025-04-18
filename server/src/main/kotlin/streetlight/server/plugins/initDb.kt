package streetlight.server.plugins

import klutch.db.services.UserInitService
import klutch.db.tables.RefreshTokenTable
import klutch.db.tables.UserTable
import klutch.environment.Environment
import klutch.utils.dbLog
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import streetlight.server.db.tables.AreaTable
import streetlight.server.db.tables.EventTable
import streetlight.server.db.tables.LocationTable
import streetlight.server.db.tables.RequestTable
import streetlight.server.db.tables.SongTable

fun initDb(env: Environment) {
    dbLog.logInfo("initializing db")
    val db = connectDb(env)

    transaction(db) {
        SchemaUtils.create(*dbTables.toTypedArray())
    }

    runBlocking {
        UserInitService().initUsers()
    }
}

val dbTables = listOf(
    UserTable,
    LocationTable,
    AreaTable,
    EventTable,
    SongTable,
    RequestTable,
    RefreshTokenTable,
)

fun connectDb(env: Environment) = Database.connect(
    url = "jdbc:postgresql://localhost:5432/streetlightdb",
    driver = "org.postgresql.Driver",
    user = "streetlight",
    password = env.read("PSQL_PW")
)