package streetlight.server.db.tables

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.EventTag
import streetlight.model.data.EventTagId

object EventTagTable : IdTable<String>("event_tag") {
    override val id = text("id").entityId()
    val name = text("name")
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toEventTag() = EventTag(
    eventTagId = EventTagId(this[EventTagTable.id].value),
    name = this[EventTagTable.name],
    description = this[EventTagTable.description]
)

fun UpdateBuilder<*>.writeFull(eventTag: EventTag) {
    this[EventTagTable.id] = eventTag.eventTagId.value
    writeUpdate(eventTag)
}

fun UpdateBuilder<*>.writeUpdate(eventTag: EventTag) {
    this[EventTagTable.name] = eventTag.name
    this[EventTagTable.description] = eventTag.description
}
