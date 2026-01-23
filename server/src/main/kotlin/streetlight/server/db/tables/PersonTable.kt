package streetlight.server.db.tables

import klutch.db.tables.UserTable
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Person
import streetlight.model.data.PersonId
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toUserId

object PersonTable : UUIDTable("person") {
    val userId = reference("user_id", UserTable, ReferenceOption.CASCADE)
    val venmo = text("venmo")
    val stageName = text("stage_name")
}

fun ResultRow.toSpark() = Person(
    personId = toProjectId<PersonId>(PersonTable.id),
    userId = toUserId(PersonTable.userId),
    venmo = this[PersonTable.venmo],
    stageName = this[PersonTable.stageName],
)

// Updaters
fun UpdateBuilder<*>.writeFull(person: Person) {
    this[PersonTable.id] = person.personId.value.toUUID()
    this[PersonTable.userId] = person.userId.value.toUUID()
    writeUpdate(person)
}

fun UpdateBuilder<*>.writeUpdate(person: Person) {
    this[PersonTable.venmo] = person.venmo
    this[PersonTable.stageName] = person.stageName
}
