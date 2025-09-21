package streetlight.server.plugins

import kabinet.utils.Environment
import klutch.db.services.UserInitService
import klutch.db.tables.RefreshTokenTable
import klutch.db.tables.UserTable
import klutch.utils.dbLog
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import streetlight.server.ServerProvider
import streetlight.server.db.tables.AreaTable
import streetlight.server.db.tables.EventTable
import streetlight.server.db.tables.LocationTable
import streetlight.server.db.tables.RequestTable
import streetlight.server.db.tables.SongTable

fun initDb(
    app: ServerProvider = ServerProvider
) {
    dbLog.logInfo("initializing db")
    val db = connectDb(app.env)

    transaction(db) {
        SchemaUtils.create(*dbTables.toTypedArray())
    }

    runBlocking {
        UserInitService(app.env).initUsers()
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