package streetlight.server.db.tables

import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Street
import streetlight.server.utils.toProjectId

object StreetTable : UUIDTable("street") {
    val name = text("name")
}

fun ResultRow.toArea() = Street(
    streetId = toProjectId(StreetTable.id),
    name = this[StreetTable.name]
)

// Updaters
fun UpdateBuilder<*>.writeFull(street: Street) {
    this[StreetTable.id] = street.streetId.toUUID()
    writeUpdate(street)
}

fun UpdateBuilder<*>.writeUpdate(street: Street) {
    this[StreetTable.name] = street.name
}

