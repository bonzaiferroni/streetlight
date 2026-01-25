package streetlight.server.db.services

import kampfire.model.UserId
import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.toStringId
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import streetlight.model.data.Person
import streetlight.model.data.PersonId
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.PersonTable
import streetlight.server.db.tables.toSpark
import streetlight.server.db.tables.writeFull
import streetlight.server.db.tables.writeUpdate

class SparkTableDao: DbService() {

    // Spark CRUD
    suspend fun readById(personId: PersonId) = dbQuery {
        PersonTable.read { it.id.eq(personId) }.firstOrNull()?.toSpark()
    }

    suspend fun readByUserId(userId: UserId) = dbQuery {
        PersonTable.read { it.userId.eq(userId) }.firstOrNull()?.toSpark()
    }

    suspend fun createSpark(person: Person): PersonId = dbQuery {
        PersonTable.insertAndGetId {
            it.writeFull(person)
        }.value.toStringId().toProjectId()
    }

    suspend fun updateSpark(person: Person) = dbQuery {
        PersonTable.update(where = { PersonTable.id.eq(person.personId) }) {
            it.writeUpdate(person)
        } == 1
    }
}
