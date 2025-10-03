package streetlight.server.db.tables

import kabinet.model.UserId
import klutch.db.tables.UserTable
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Spark
import streetlight.model.data.SparkId
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toUserId

object SparkTable : UUIDTable("spark") {
    val userId = reference("user_id", UserTable, ReferenceOption.CASCADE)
    val venmo = text("venmo")
    val stageName = text("stage_name")
}

fun ResultRow.toSpark() = Spark(
    sparkId = toProjectId<SparkId>(SparkTable.id),
    userId = toUserId(SparkTable.userId),
    venmo = this[SparkTable.venmo],
    stageName = this[SparkTable.stageName],
)

// Updaters
fun UpdateBuilder<*>.writeFull(spark: Spark) {
    this[SparkTable.id] = spark.sparkId.value.toUUID()
    this[SparkTable.userId] = spark.userId.value.toUUID()
    writeUpdate(spark)
}

fun UpdateBuilder<*>.writeUpdate(spark: Spark) {
    this[SparkTable.venmo] = spark.venmo
    this[SparkTable.stageName] = spark.stageName
}
