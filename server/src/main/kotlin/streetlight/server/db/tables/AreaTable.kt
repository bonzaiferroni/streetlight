package streetlight.server.db.tables

import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Area
import streetlight.model.data.AreaId
import streetlight.server.utils.toProjectId

object AreaTable : UUIDTable() {
    val name = text("name")
}

fun ResultRow.toArea() = Area(
    areaId = toProjectId(AreaTable.id),
    name = this[AreaTable.name]
)

// Updaters
fun UpdateBuilder<*>.writeFull(area: Area) {
    this[AreaTable.id] = area.areaId.toUUID()
    writeUpdate(area)
}

fun UpdateBuilder<*>.writeUpdate(area: Area) {
    this[AreaTable.name] = area.name
}

