package streetlight.server.db.services

import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.read
import klutch.db.readById
import klutch.db.tables.UserTable
import klutch.db.tables.toUser
import klutch.utils.eq
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import streetlight.model.data.Spark
import streetlight.model.data.SparkId
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.SparkTable
import streetlight.server.db.tables.toSpark
import streetlight.server.db.tables.writeFull
import streetlight.server.db.tables.writeUpdate

class SparkTableDao: DbService() {

    // Spark CRUD
    suspend fun readById(sparkId: SparkId) = dbQuery {
        SparkTable.read { it.id.eq(sparkId) }.firstOrNull()?.toSpark()
    }

    suspend fun readByUserId(userId: UserId) = dbQuery {
        SparkTable.read { it.userId.eq(userId) }.firstOrNull()?.toSpark()
    }

    suspend fun createSpark(spark: Spark): SparkId = dbQuery {
        SparkTable.insertAndGetId {
            it.writeFull(spark)
        }.value.toStringId().toProjectId()
    }

    suspend fun updateSpark(spark: Spark) = dbQuery {
        SparkTable.update(where = { SparkTable.id.eq(spark.sparkId) }) {
            it.writeUpdate(spark)
        } == 1
    }
}
