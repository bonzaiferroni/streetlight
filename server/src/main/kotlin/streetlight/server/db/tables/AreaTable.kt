package streetlight.server.db.tables

import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import streetlight.model.data.Area
import streetlight.model.data.AreaId
import streetlight.server.utils.toProjectId

internal object AreaTable : UUIDTable() {
    val name = text("name")
}

internal fun ResultRow.toArea() = Area(
    areaId = toProjectId(AreaTable.id),
    name = this[AreaTable.name]
)